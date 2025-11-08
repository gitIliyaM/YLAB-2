package YLab.service;

import YLab.audit.AuditLog;
import YLab.model.Product;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataService {
    private static final String DATA_DIR = "data";
    private static final String PRODUCTS_FILE = DATA_DIR + "/products.csv";
    private static final String AUDIT_FILE = DATA_DIR + "/audit.log";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    public void saveProducts(List<Product> products) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product p : products) {
                String safeDesc = p.getDescription().replace(",", ";");
                writer.println(String.join(",",
                        p.getId(),
                        p.getName(),
                        p.getCategory(),
                        p.getBrand(),
                        String.valueOf(p.getPrice()),
                        safeDesc
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
        }
    }

    public List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) {
            return products;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length == 6) {
                    String id = parts[0];
                    String name = parts[1];
                    String category = parts[2];
                    String brand = parts[3];
                    double price = Double.parseDouble(parts[4]);
                    String description = parts[5].replace(";", ",");
                    products.add(new Product(id, name, category, brand, price, description));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        return products;
    }

    public void saveAudit(List<AuditLog> logs) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(AUDIT_FILE))) {
            for (AuditLog log : logs) {
                writer.println(log.getTimestamp() + "|" + log.getAction() + "|" + log.getDetails());
            }
        } catch (IOException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> loadAudit() {
        List<AuditLog> logs = new ArrayList<>();
        File file = new File(AUDIT_FILE);
        if (!file.exists()) {
            return logs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    long timestamp = Long.parseLong(parts[0]);
                    String action = parts[1];
                    String details = parts[2];
                    AuditLog log = new AuditLog(action, details);
                    log.setTimestamp(timestamp);
                    logs.add(log);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading audit log: " + e.getMessage());
        }
        return logs;
    }
}