package y_lab.service;

import y_lab.audit.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditServiceJdbc {

    private final Connection connection;

    public AuditServiceJdbc(Connection connection) {
        this.connection = connection;
    }

    public void log(String action, String details) throws SQLException {
        String sql = "INSERT INTO app_schema.audit_log (action, details, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, action);
            stmt.setString(2, details);
            stmt.setLong(3, System.currentTimeMillis());

            stmt.executeUpdate();
            System.out.println("Audit: [" + new java.util.Date() + "] " + action + ": " + details);
        }
    }

    public void printAllLogs() throws SQLException {
        System.out.println("=== Audit ===");
        String sql = "SELECT action, details, timestamp FROM app_schema.audit_log ORDER BY timestamp ASC";
        List<AuditLog> logs = new ArrayList<>();
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getLong("timestamp")
                ));
            }
        }

        if (logs.isEmpty()) {
            System.out.println("There are no records");
        } else {
            logs.forEach(System.out::println);
        }
    }

    public List<AuditLog> getAllLogs() throws SQLException {
        String sql = "SELECT action, details, timestamp FROM app_schema.audit_log ORDER BY timestamp ASC";
        List<AuditLog> logs = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getLong("timestamp")
                ));
            }
        }
        return logs;
    }
}