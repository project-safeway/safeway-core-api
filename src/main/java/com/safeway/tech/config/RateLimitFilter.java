package com.safeway.tech.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

public class RateLimitFilter implements Filter {

    private static final int MAX_BUCKETS = 10_000;
    private static final long CAPACITY = 5;
    private static final long REFILL_TOKENS = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    private static final long IDLE_TIMEOUT_NANOS =
            REFILL_PERIOD.multipliedBy(CAPACITY).dividedBy(REFILL_TOKENS).toNanos();

    private final LinkedHashMap<String, BucketEntry> buckets = new LinkedHashMap<>(16, 0.75f, true);
    private final int maxBuckets;
    private final TimeMeter timeMeter;

    public RateLimitFilter() {
        this(MAX_BUCKETS, TimeMeter.SYSTEM_NANOTIME);
    }

    RateLimitFilter(int maxBuckets, TimeMeter timeMeter) {
        if (maxBuckets <= 0) {
            throw new IllegalArgumentException("maxBuckets must be positive");
        }
        this.maxBuckets = maxBuckets;
        this.timeMeter = Objects.requireNonNull(timeMeter);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String ip = httpRequest.getRemoteAddr();

        if (tryConsume(ip)) {
            chain.doFilter(servletRequest, servletResponse);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"error\": \"Too many requests. Please try again later.\"}"
            );
        }
    }

    private synchronized boolean tryConsume(String ip) {
        long now = timeMeter.currentTimeNanos();
        Iterator<BucketEntry> entries = buckets.values().iterator();
        while (entries.hasNext()) {
            if (now - entries.next().lastAccessNanos() < IDLE_TIMEOUT_NANOS) {
                break;
            }
            entries.remove();
        }

        BucketEntry entry = buckets.get(ip);
        if (entry == null) {
            if (buckets.size() >= maxBuckets) {
                return false;
            }
            entry = new BucketEntry(createNewBucket(), now);
        }

        boolean consumed = entry.bucket().tryConsume(1);
        // Retain the bucket for a full refill after consumption, including denied attempts.
        buckets.put(ip, new BucketEntry(entry.bucket(), timeMeter.currentTimeNanos()));
        return consumed;
    }

    private Bucket createNewBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(CAPACITY)
                .refillGreedy(REFILL_TOKENS, REFILL_PERIOD)
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .withCustomTimePrecision(timeMeter)
                .build();
    }

    private record BucketEntry(Bucket bucket, long lastAccessNanos) {
    }
}
