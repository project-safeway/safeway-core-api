package com.safeway.tech.infra.exception;

public class InvitationExpiredException extends RuntimeException {

    public InvitationExpiredException() {
        super("Invitation has expired");
    }
}
