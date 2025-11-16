package y_lab.repository;

import y_lab.config.LiquibaseTestUtil;
import y_lab.model.Product;
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
public class ProductRepositoryJdbcTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static Connection connection;
    private ProductRepositoryJdbc repository;

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
    void setUpEach() {
        repository = new ProductRepositoryJdbc(connection);
    }

    @AfterEach
    void cleanUpEach() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM app_schema.products");
            stmt.execute("DELETE FROM app_schema.audit_log");
        }
    }

    @Test
    void saveAndFindById() throws Exception {
        String name = "Test Laptop";
        String category = "Electronics";
        String brand = "TestBrand";
        double price = 1234.56;
        String desc = "A test laptop";

        long id = repository.save(name, category, brand, price, desc);
        Product found = repository.findById(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals(name, found.getName());
        assertEquals(category, found.getCategory());
        assertEquals(brand, found.getBrand());
        assertEquals(price, found.getPrice(), 0.01);
        assertEquals(desc, found.getDescription());
    }

    @Test
    void findAll_emptyInitially() throws Exception {
        List<Product> products = repository.findAll();
        assertTrue(products.isEmpty());
    }

    @Test
    void findAll_withData() throws Exception {
        repository.save("P1", "Cat1", "B1", 10.0, "D1");
        repository.save("P2", "Cat2", "B2", 20.0, "D2");

        List<Product> products = repository.findAll();
        assertEquals(2, products.size());
    }

    @Test
    void updateProduct() throws Exception {
        long id = repository.save("Old", "OldCat", "OldBrand", 1.0, "OldDesc");
        boolean updated = repository.update(id, "New", "NewCat", "NewBrand", 99.0, "NewDesc");

        assertTrue(updated);
        Product p = repository.findById(id);
        assertEquals("New", p.getName());
        assertEquals(99.0, p.getPrice(), 0.01);
    }

    @Test
    void deleteProduct() throws Exception {
        long id = repository.save("To delete", "Cat", "Brand", 1.0, "Desc");
        boolean deleted = repository.deleteById(id);

        assertTrue(deleted);
        assertNull(repository.findById(id));
    }

    @Test
    void findByCategory() throws Exception {
        repository.save("P1", "Electronics", "B1", 10.0, "D1");
        repository.save("P2", "Books", "B2", 5.0, "D2");
        repository.save("P3", "Electronics", "B3", 15.0, "D3");

        List<Product> electronics = repository.findByCategory("Electronics");
        assertEquals(2, electronics.size());
        assertTrue(electronics.stream().allMatch(p -> "Electronics".equals(p.getCategory())));
    }

    @Test
    void size() throws Exception {
        assertEquals(0, repository.size());
        repository.save("P", "C", "B", 1.0, "D");
        assertEquals(1, repository.size());
    }
}
