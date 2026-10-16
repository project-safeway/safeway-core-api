package com.safeway.tech.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class FlywayBaselineConfiguration {

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer() {
        return configuration -> configuration
                .locations("classpath:db.migration")
                .baselineVersion("1.0.0");
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                if (flyway.info().applied().length == 0 && hasApplicationTables(flyway)) {
                    dropSchemaMarkerHistoryTable(flyway);
                    flyway.baseline();
                }
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to inspect the database schema before Flyway migration",
                        exception);
            }
            flyway.migrate();
        };
    }

    private boolean hasApplicationTables(Flyway flyway) throws SQLException {
        try (Connection connection = flyway.getConfiguration().getDataSource().getConnection()) {
            String historyTable = flyway.getConfiguration().getTable();
            try (ResultSet tables = connection.getMetaData().getTables(
                    connection.getCatalog(),
                    connection.getSchema(),
                    null,
                    new String[]{"TABLE"})) {
                while (tables.next()) {
                    if (!historyTable.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void dropSchemaMarkerHistoryTable(Flyway flyway) throws SQLException {
        try (Connection connection = flyway.getConfiguration().getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String historyTable = flyway.getConfiguration().getTable();
            try (ResultSet tables = metaData.getTables(
                    connection.getCatalog(),
                    connection.getSchema(),
                    null,
                    new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (historyTable.equalsIgnoreCase(tableName)) {
                        String qualifiedTable = qualifyTable(metaData, tables);
                        String typeColumn = findColumnName(metaData, tables, "TYPE");
                        String qualifiedTypeColumn = quoteIdentifier(
                                metaData.getIdentifierQuoteString().trim(), typeColumn);
                        try (Statement statement = connection.createStatement();
                                ResultSet rowCount = statement.executeQuery(
                                        "SELECT COUNT(*) FROM " + qualifiedTable
                                                + " WHERE " + qualifiedTypeColumn
                                                + " IS NULL OR " + qualifiedTypeColumn
                                                + " NOT IN ('SCHEMA', 'TABLE')")) {
                            rowCount.next();
                            long unexpectedRowCount = rowCount.getLong(1);
                            if (unexpectedRowCount != 0) {
                                throw new IllegalStateException(
                                        "Flyway history table contains migration records, but no applied migrations "
                                                + "were found");
                            }
                        }
                        try (Statement statement = connection.createStatement()) {
                            statement.execute("DROP TABLE " + qualifiedTable);
                        }
                        return;
                    }
                }
            }
        }
    }

    private String findColumnName(DatabaseMetaData metaData, ResultSet table, String expectedName)
            throws SQLException {
        try (ResultSet columns = metaData.getColumns(
                table.getString("TABLE_CAT"),
                table.getString("TABLE_SCHEM"),
                table.getString("TABLE_NAME"),
                null)) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (expectedName.equalsIgnoreCase(columnName)) {
                    return columnName;
                }
            }
        }
        throw new IllegalStateException("Flyway history table does not contain the expected type column");
    }

    private String qualifyTable(DatabaseMetaData metaData, ResultSet table) throws SQLException {
        String quote = metaData.getIdentifierQuoteString().trim();
        String tableName = quoteIdentifier(quote, table.getString("TABLE_NAME"));
        String schema = table.getString("TABLE_SCHEM");
        String catalog = table.getString("TABLE_CAT");
        String qualifier = schema != null ? schema : catalog;
        if (qualifier == null || qualifier.isBlank()) {
            return tableName;
        }
        return quoteIdentifier(quote, qualifier) + "." + tableName;
    }

    private String quoteIdentifier(String quote, String identifier) {
        if (quote.isEmpty()) {
            return identifier;
        }
        return quote + identifier.replace(quote, quote + quote) + quote;
    }
}
