package com.safeway.tech.auth.core.port;

import java.util.UUID;

public interface UpdateUserPasswordPort {

    void updatePasswordHash(UUID userId, String passwordHash);
}
