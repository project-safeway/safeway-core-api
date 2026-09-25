package com.safeway.tech.infra.exception;

public class RouteSchoolNotFound extends RuntimeException {
    public RouteSchoolNotFound(String message) {
        super(message);
    }
}
