package com.eazybytes.constant;

public final class SecurityRoutes {

    private final static String[] PUBLIC_ROUTES = {
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/reset-password",
            "/api/auth/request-reset-password-token"
    };

    private SecurityRoutes() {
    }

    public static String[] getPublicRoutes() {
        return PUBLIC_ROUTES;
    }
}