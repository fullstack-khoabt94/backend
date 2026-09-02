package com.eazybytes.api;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The one Postgres container every API flow test shares.
 *
 * An interface rather than a base class so tests do not spend their single
 * `extends` slot on it, and because interface fields are already
 * `public static final`.
 *
 * Boot's @ImportTestcontainers reads the static field, starts the container and
 * wires @ServiceConnection, so the random host port reaches the DataSource
 * without any hand-written property plumbing. Sharing one instance keeps the
 * JDBC URL stable across test classes, which is what lets Spring reuse its
 * cached ApplicationContext instead of rebuilding Hibernate and re-running
 * Flyway for every class.
 */
public interface ContainerConfig {

    @ServiceConnection
    PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.2-alpine");
}
