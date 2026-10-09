package com.wmrt.dispatch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Configures SQLite-specific settings after the DataSource is ready.
 * Enables WAL (Write-Ahead Logging) journal mode for better concurrency.
 */
@Component
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private final DataSource dataSource;

    @Autowired
    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void configureSQLite() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
            log.info("SQLite configured: WAL journal mode enabled, foreign keys ON");
        } catch (Exception e) {
            log.warn("Could not configure SQLite PRAGMAs: {}", e.getMessage());
        }
    }
}
