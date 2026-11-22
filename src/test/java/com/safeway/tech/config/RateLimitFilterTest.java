package com.safeway.tech.config;

import io.github.bucket4j.TimeMeter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    private final AtomicLong nanos = new AtomicLong();
    private final TimeMeter timeMeter = new TimeMeter() {
        @Override
        public long currentTimeNanos() {
            return nanos.get();
        }

        @Override
        public boolean isWallClockBased() {
            return false;
        }
    };
    private final RateLimitFilter filter = new RateLimitFilter(2, timeMeter);

    @Test
    void preservesCapacityAndGreedyRefill() throws Exception {
        exhaust("client");
        assertFalse(request("client"));

        nanos.set(Duration.ofSeconds(6).toNanos() - 1);
        assertFalse(request("client"));
        nanos.incrementAndGet();
        assertTrue(request("client"));
        assertFalse(request("client"));
    }

    @Test
    void rejectsNewAddressesWhenFullWithoutResettingExistingBuckets() throws Exception {
        exhaust("first");
        assertTrue(request("second"));

        for (int i = 0; i < 100; i++) {
            assertFalse(request("new-" + i));
        }

        assertFalse(request("first"));
        for (int i = 0; i < 4; i++) {
            assertTrue(request("second"));
        }
        assertFalse(request("second"));
    }

    @Test
    void onlyReclaimsStorageAfterFullRefillTime() throws Exception {
        exhaust("first");
        exhaust("second");

        nanos.set(Duration.ofSeconds(30).toNanos() - 1);
        assertFalse(request("third"));

        nanos.incrementAndGet();
        assertTrue(request("third"));
        exhaust("first");
        assertFalse(request("first"));
    }

    @Test
    void refreshesIdleExpirationAndRetainsRecentlyUsedBuckets() throws Exception {
        exhaust("first");
        exhaust("second");

        nanos.set(Duration.ofSeconds(6).toNanos());
        assertTrue(request("first"));
        nanos.set(Duration.ofSeconds(30).toNanos());
        assertTrue(request("third"));

        assertFalse(request("fourth"));
        // Four tokens were refilled since the last successful attempt, not five.
        for (int i = 0; i < 4; i++) {
            assertTrue(request("first"));
        }
        assertFalse(request("first"));
    }

    @Test
    void deniedAttemptsAlsoRefreshIdleExpiration() throws Exception {
        exhaust("first");
        exhaust("second");
        nanos.set(Duration.ofSeconds(1).toNanos());
        assertFalse(request("first"));
        assertFalse(request("second"));

        nanos.set(Duration.ofSeconds(30).toNanos());
        assertFalse(request("third"));
        nanos.set(Duration.ofSeconds(31).toNanos());
        assertTrue(request("third"));
    }

    @Test
    void concurrentRequestsCannotExceedClientCapacity() throws Exception {
        List<Callable<Boolean>> requests = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            requests.add(() -> request("client"));
        }
        assertEquals(5, countAccepted(requests));
    }

    @Test
    void concurrentAddressesCannotExceedStorageCapacity() throws Exception {
        List<Callable<Boolean>> requests = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            String ip = "client-" + i;
            requests.add(() -> request(ip));
        }
        assertEquals(2, countAccepted(requests));
    }

    private long countAccepted(List<Callable<Boolean>> requests) throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            long accepted = 0;
            for (var result : executor.invokeAll(requests)) {
                if (result.get()) {
                    accepted++;
                }
            }
            return accepted;
        }
    }

    private void exhaust(String ip) throws Exception {
        for (int i = 0; i < 5; i++) {
            assertTrue(request(ip));
        }
    }

    private boolean request(String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> res.getWriter().write("accepted"));
        if (response.getStatus() == 429) {
            assertEquals("application/json", response.getContentType());
            assertEquals("{\"error\": \"Too many requests. Please try again later.\"}", response.getContentAsString());
            return false;
        }
        assertEquals(200, response.getStatus());
        assertEquals("accepted", response.getContentAsString());
        return true;
    }
}
