package y_lab.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.testcontainers.containers.PostgreSQLContainer;
import java.sql.Connection;

public class LiquibaseTestUtil {

    public static void runMigrations(PostgreSQLContainer<?> container) {
        try (Connection connection = container.createConnection("")) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try (var stmt = connection.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS app_schema");
            }

            database.setLiquibaseSchemaName("app_schema");
            database.setDefaultSchemaName("app_schema");

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            var stream = LiquibaseTestUtil.class.getClassLoader()
                    .getResourceAsStream("db/changelog/db.changelog-master.xml");
            if (stream == null) {
                throw new IllegalStateException("Changelog not found in classpath!");
            } else  {
                System.out.printf("Changelog found in classpath!");
            }

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException("Failed to run Liquibase migrations in test container", e);
        }
    }
}
