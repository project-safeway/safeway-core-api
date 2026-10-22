package com.safeway.tech.repository;

import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.DriverManager;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserUuidPersistenceTest {

    @ParameterizedTest
    @ValueSource(strings = {"org.hibernate.dialect.MySQLDialect", "org.hibernate.dialect.H2Dialect"})
    void findsRegisteredUserByUuidWithCharColumnsInH2(String dialect) throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL";
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE transports (
                        id CHAR(36) PRIMARY KEY, active BOOLEAN NOT NULL,
                        created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
                        deleted_at DATETIME, license_plate VARCHAR(7) NOT NULL,
                        model VARCHAR(255), capacity INT
                    )
                    """);
            statement.execute("""
                    CREATE TABLE users (
                        id CHAR(36) PRIMARY KEY, active BOOLEAN NOT NULL,
                        created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
                        deleted_at DATETIME, name VARCHAR(100) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE, password_hash TEXT NOT NULL,
                        role ENUM('ADMIN', 'DRIVER', 'GUARDIAN') NOT NULL,
                        primary_phone_number CHAR(15) NOT NULL,
                        secondary_phone_number CHAR(15),
                        transport_id CHAR(36) REFERENCES transports(id)
                    )
                    """);

            Configuration configuration = new Configuration()
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Transport.class)
                    .setProperty("hibernate.connection.url", url)
                    .setProperty("hibernate.connection.username", "sa")
                    .setProperty("hibernate.connection.password", "")
                    .setProperty("hibernate.physical_naming_strategy",
                            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                    .setProperty("hibernate.dialect", dialect)
                    .setProperty("hibernate.hbm2ddl.auto", "none");

            try (var factory = configuration.buildSessionFactory()) {
                UUID userId;
                UUID transportId;
                try (var session = factory.openSession()) {
                    var transaction = session.beginTransaction();
                    Transport transport = new Transport();
                    transport.setLicensePlate("ABC1234");
                    transport.setCreatedAt(Instant.now());
                    transport.setUpdatedAt(Instant.now());
                    session.persist(transport);
                    User user = new User();
                    user.setName("Test User");
                    user.setEmail("uuid-test@example.com");
                    user.setPasswordHash("unused");
                    user.setRole(UserRole.DRIVER);
                    user.setPrimaryPhoneNumber("11999999999");
                    user.setCreatedAt(Instant.now());
                    user.setUpdatedAt(Instant.now());
                    user.setTransport(transport);
                    session.persist(user);
                    transaction.commit();
                    userId = user.getId();
                    transportId = transport.getId();
                }

                try (var session = factory.openSession()) {
                    User loginUser = session.createQuery("from User where email = :email", User.class)
                            .setParameter("email", "uuid-test@example.com")
                            .getSingleResult();
                    assertEquals(userId, loginUser.getId());
                }
                try (var session = factory.openSession()) {
                    User currentUser = session.find(User.class, userId);
                    assertNotNull(currentUser);
                    assertNotNull(currentUser.getTransport());
                    assertEquals(transportId, currentUser.getTransport().getId());
                }
                try (var rows = statement.executeQuery("SELECT id, transport_id FROM users")) {
                    rows.next();
                    assertEquals(userId.toString(), rows.getString("id"));
                    assertEquals(transportId.toString(), rows.getString("transport_id"));
                }
            }
        }
    }
}
