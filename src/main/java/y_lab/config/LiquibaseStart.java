package y_lab.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class LiquibaseStart {

    public static void runMigrations() {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = LiquibaseStart.class
                .getClassLoader()
                .getResourceAsStream("application.yml")) {

            if (inputStream == null) {
                throw new RuntimeException("Cannot find application.yml");
            }

            Map<String, Object> config = yaml.load(inputStream);
            if (config == null) {
                throw new RuntimeException("Configuration file application.yml is empty or invalid.");
            }

            Map<String, Object> dbConfig = (Map<String, Object>) config.get("db");
            if (dbConfig == null) {
                throw new RuntimeException("Configuration section 'db' not found in application.yml");
            }

            String url = (String) dbConfig.get("url");
            String username = (String) dbConfig.get("username");
            String password = (String) dbConfig.get("password");

            if (url == null || username == null || password == null) {
                throw new RuntimeException("Database URL, username, or password not found under 'db' section");
            }

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Connected to database for Liquibase migrations.");

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS app_schema");
                }

                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));

                database.setLiquibaseSchemaName("app_schema");
                database.setDefaultSchemaName("app_schema");

                Liquibase liquibase = new Liquibase(
                        "db/changelog/db.changelog-master.xml",
                        new ClassLoaderResourceAccessor(),
                        database
                );

                liquibase.update(new Contexts(), new LabelExpression());
                System.out.println("Liquibase migrations completed successfully.");

            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                throw new RuntimeException("Failed to connect to database for Liquibase", e);
            } catch (LiquibaseException e) {
                System.err.println("Liquibase migration error: " + e.getMessage());
                throw new RuntimeException("Liquibase migration failed", e);
            }

        } catch (Exception e) {
            System.err.println("Configuration or runtime error: " + e.getMessage());
            throw new RuntimeException("Failed to run Liquibase migrations", e);
        }
    }
}