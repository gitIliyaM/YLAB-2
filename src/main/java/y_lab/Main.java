package y_lab;

import y_lab.config.Config;
import y_lab.repository.ProductRepositoryJdbc;
import y_lab.service.AuditServiceJdbc;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        y_lab.config.LiquibaseStart.runMigrations();

        Config config = new Config("application.yml");
        DataSource dataSource = config.getDataSource();

        try (Connection connection = dataSource.getConnection()) {
            ProductRepositoryJdbc productRepository = new ProductRepositoryJdbc(connection);
            AuditServiceJdbc auditService = new AuditServiceJdbc(connection);

            ConsoleUI ui = new ConsoleUI(productRepository, auditService);
            ui.start();

            System.out.println("Program terminated. Data remains in the database.");
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}