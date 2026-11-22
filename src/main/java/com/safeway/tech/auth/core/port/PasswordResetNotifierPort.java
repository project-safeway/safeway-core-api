package com.safeway.tech.auth.core.port;

public interface PasswordResetNotifierPort {

    void notifyCode(String email, String code, long expiresInMinutes);
}
