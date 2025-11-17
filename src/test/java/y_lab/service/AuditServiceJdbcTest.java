package y_lab.service;

import y_lab.audit.AuditLog;
import y_lab.config.LiquibaseTestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class AuditServiceJdbcTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static Connection connection;
    private AuditServiceJdbc auditService;

    @BeforeAll
    static void setUpAll() throws Exception {
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        LiquibaseTestUtil.runMigrations(postgres);
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @BeforeEach
    void setUpEach() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM app_schema.audit_log");
        }
        auditService = new AuditServiceJdbc(connection);
    }

    @AfterEach
    void cleanUpEach() throws Exception {
    }

    @Test
    void logAndRetrieve() throws Exception {
        auditService.log("TEST_ACTION", "Test details");

        List<AuditLog> logs = auditService.getAllLogs();
        assertEquals(1, logs.size());
        AuditLog log = logs.get(0);
        assertEquals("TEST_ACTION", log.getAction());
        assertEquals("Test details", log.getDetails());
        assertTrue(log.getTimestamp() > 0);
    }
}
