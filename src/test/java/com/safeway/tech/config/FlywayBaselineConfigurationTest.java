package com.safeway.tech.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlywayBaselineConfigurationTest {

    @Test
    void baselinesExistingSchemaWhenHistoryHasNoAppliedMigrations() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:flyway-baseline-test;DB_CLOSE_DELAY=-1",
                "sa",
                "");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:empty-migrations")
                .baselineVersion("1.0.0")
                .load();
        flyway.migrate();

        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE legacy_user (id INT PRIMARY KEY)");
        }

        new FlywayBaselineConfiguration().flywayMigrationStrategy().migrate(flyway);

        assertEquals("1.0.0", flyway.info().current().getVersion().getVersion());
    }
}
