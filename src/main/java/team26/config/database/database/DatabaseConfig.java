package team26.config.database.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    public static void init() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getEnv("DB_URL", "jdbc:postgresql://localhost:7432/testDB"));
        hikariConfig.setUsername(getEnv("DB_USER_NAME", "postgres"));
        hikariConfig.setPassword(getEnv("DB_USER_PASSWORD", "postgres"));
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(hikariConfig);

        runFlywayMigrations();

        logger.info("Database initialization completed successfully");
    }

    private static void runFlywayMigrations() {
        logger.info("Starting Flyway migrations...");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load();

        MigrationInfoService info = flyway.info();

        if (info.current() != null) {
            logger.info("Current schema version: {}", info.current().getVersion());
        } else {
            logger.info("No existing schema - first migration");
        }
        // Выполняем миграции
        MigrateResult appliedMigrations = flyway.migrate();
        logger.info("Applied {} migration(s)", appliedMigrations);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value != null ? value : defaultValue;
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("DataSource closed");
        }
    }
}
