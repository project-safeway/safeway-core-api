package com.safeway.tech.auth.core.port;

import com.safeway.tech.auth.core.model.RegisterAuthUserData;
import com.safeway.tech.auth.core.model.RegisteredAuthUser;

public interface RegisterAuthUserPort {
    boolean existsByEmail(String email);
    boolean existsByLicensePlate(String licensePlate);
    RegisteredAuthUser create(RegisterAuthUserData data);
}
