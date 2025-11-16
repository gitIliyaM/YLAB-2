package y_lab.repository;

import y_lab.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryJdbc {

    private final Connection connection;

    public ProductRepositoryJdbc(Connection connection) {
        this.connection = connection;
    }

    public long save(String name, String category, String brand, double price, String description) throws SQLException {
        String sql = "INSERT INTO app_schema.products (name, category, brand, price, description) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setString(3, brand);
            stmt.setDouble(4, price);
            stmt.setString(5, description);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            } else {
                throw new SQLException("Creating product failed, no ID obtained.");
            }
        }
    }

    public Product findById(long id) throws SQLException {
        String sql = "SELECT id, name, category, brand, price, description FROM app_schema.products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToProduct(rs);
            }
        }
        return null;
    }

    public List<Product> findAll() throws SQLException {
        String sql = "SELECT id, name, category, brand, price, description FROM app_schema.products";
        List<Product> products = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> findByCategory(String category) throws SQLException {
        String sql = "SELECT id, name, category, brand, price, description FROM app_schema.products WHERE category = ?";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> findByBrand(String brand) throws SQLException {
        String sql = "SELECT id, name, category, brand, price, description FROM app_schema.products WHERE brand = ?";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, brand);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> findByPriceRange(double min, double max) throws SQLException {
        String sql = "SELECT id, name, category, brand, price, description FROM app_schema.products WHERE price BETWEEN ? AND ?";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public boolean update(long id, String name, String category, String brand, double price, String description) throws SQLException {
        String sql = "UPDATE app_schema.products SET name = ?, category = ?, brand = ?, price = ?, description = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setString(3, brand);
            stmt.setDouble(4, price);
            stmt.setString(5, description);
            stmt.setLong(6, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM app_schema.products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public int size() throws SQLException {
        String sql = "SELECT COUNT(*) FROM app_schema.products";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("brand"),
                rs.getDouble("price"),
                rs.getString("description")
        );
    }
}