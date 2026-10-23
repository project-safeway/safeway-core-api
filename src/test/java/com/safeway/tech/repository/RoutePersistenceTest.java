package com.safeway.tech.repository;

import com.safeway.tech.domain.enums.RouteTypeEnum;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.Transport;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.DriverManager;
import java.sql.Time;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutePersistenceTest {

    @ParameterizedTest
    @ValueSource(strings = {"org.hibernate.dialect.MySQLDialect", "org.hibernate.dialect.H2Dialect"})
    void persistsAndReadsRouteTypesAsNames(String dialect) throws Exception {
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
                    CREATE TABLE routes (
                        id CHAR(36) PRIMARY KEY, active BOOLEAN NOT NULL,
                        created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
                        deleted_at DATETIME, name VARCHAR(150), start_time TIME, end_time TIME,
                        route_type ENUM('TO_SCHOOL', 'FROM_SCHOOL') NOT NULL,
                        transport_id CHAR(36) NOT NULL REFERENCES transports(id)
                    )
                    """);

            Configuration configuration = new Configuration()
                    .addAnnotatedClass(Route.class)
                    .addAnnotatedClass(Transport.class)
                    .setProperty("hibernate.connection.url", url)
                    .setProperty("hibernate.connection.username", "sa")
                    .setProperty("hibernate.connection.password", "")
                    .setProperty("hibernate.physical_naming_strategy",
                            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                    .setProperty("hibernate.dialect", dialect)
                    .setProperty("hibernate.hbm2ddl.auto", "none");

            try (var factory = configuration.buildSessionFactory()) {
                for (RouteTypeEnum routeType : RouteTypeEnum.values()) {
                    UUID routeId;
                    try (var session = factory.openSession()) {
                        var transaction = session.beginTransaction();
                        Transport transport = new Transport();
                        transport.setLicensePlate("ABC1234");
                        transport.setCreatedAt(Instant.now());
                        transport.setUpdatedAt(Instant.now());
                        session.persist(transport);

                        Route route = new Route();
                        route.setName("School route");
                        route.setStartTime(Time.valueOf("07:00:00"));
                        route.setEndTime(Time.valueOf("08:00:00"));
                        route.setRouteType(routeType);
                        route.setTransport(transport);
                        route.setCreatedAt(Instant.now());
                        route.setUpdatedAt(Instant.now());
                        session.persist(route);
                        transaction.commit();
                        routeId = route.getId();
                    }

                    try (var query = connection.prepareStatement(
                            "SELECT route_type FROM routes WHERE id = ?")) {
                        query.setString(1, routeId.toString());
                        try (var rows = query.executeQuery()) {
                            assertTrue(rows.next());
                            assertEquals(routeType.name(), rows.getString("route_type"));
                        }
                    }
                    try (var session = factory.openSession()) {
                        Route route = session.find(Route.class, routeId);
                        assertEquals(routeType, route.getRouteType());
                        assertEquals(Time.valueOf("07:00:00"), route.getStartTime());
                        assertEquals(Time.valueOf("08:00:00"), route.getEndTime());
                    }
                }
            }
        }
    }
}
