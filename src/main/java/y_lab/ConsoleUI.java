package y_lab;

import y_lab.model.Product;
import y_lab.service.AuditServiceJdbc;
import y_lab.repository.ProductRepositoryJdbc;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ConsoleUI {
    private static final String ADMIN_EMAIL = "user";
    private static final String ADMIN_PASSWORD = "123";
    private boolean exitRequested = false;
    private final Scanner scanner = new Scanner(System.in);
    private final ProductRepositoryJdbc productRepository;
    private final AuditServiceJdbc auditService;
    private boolean loggedIn = false;

    public ConsoleUI(ProductRepositoryJdbc productRepository, AuditServiceJdbc auditService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public void start() {
        while (true) {
            if (!loggedIn) {
                showLoginMenu();
                if (exitRequested) return;
            } else {
                showMainMenu();
                if (exitRequested) return;
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("\n=== Login ===");
        System.out.println("1. Sign in");
        System.out.println("0. Exit program");
        System.out.print("Choose: ");

        int choice = getIntInput();
        if (choice == 0) {
            exitRequested = true;
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            loggedIn = true;
            System.out.println("Login successful!");
            try {
                auditService.log("LOGIN", "User: " + email);
            } catch (Exception e) {
                System.err.println("Error logging audit: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== Product Catalog ===");
        System.out.println("1. Add product");
        System.out.println("2. View all products");
        System.out.println("3. Search by category");
        System.out.println("4. Search by brand");
        System.out.println("5. Search by price range");
        System.out.println("6. Update product");
        System.out.println("7. Delete product");
        System.out.println("8. Show audit log");
        System.out.println("9. Show metrics");
        System.out.println("0. Log out");
        System.out.print("Choose an option: ");

        int choice = getIntInput();
        switch (choice) {
            case 1 -> addProduct();
            case 2 -> listAll();
            case 3 -> searchByCategory();
            case 4 -> searchByBrand();
            case 5 -> searchByPrice();
            case 6 -> updateProduct();
            case 7 -> deleteProduct();
            case 8 -> showAuditLogs();
            case 9 -> showMetrics();
            case 0 -> {
                try {
                    auditService.log("LOGOUT", "User: " + ADMIN_EMAIL);
                } catch (Exception e) {
                    System.err.println("Error logging audit: " + e.getMessage());
                }
                System.out.println("Goodbye!");
                exitRequested = true;
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void addProduct() {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Category: ");
        String category = scanner.nextLine();
        System.out.print("Brand: ");
        String brand = scanner.nextLine();
        System.out.print("Price: ");
        double price = getDoubleInput();
        System.out.print("Description: ");
        String desc = scanner.nextLine();

        try {
            long id = productRepository.save(name, category, brand, price, desc);
            System.out.println("Product added with ID: " + id);
        } catch (Exception e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }

    private void listAll() {
        try {
            long start = System.nanoTime();
            List<Product> products = productRepository.findAll();
            long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            if (products.isEmpty()) {
                System.out.println("Catalog is empty.");
            } else {
                products.forEach(System.out::println);
            }
            System.out.println("(Query executed in " + timeMs + " ms)");
        } catch (Exception e) {
            System.err.println("Error listing products: " + e.getMessage());
        }
    }

    private void searchByCategory() {
        System.out.print("Category: ");
        String cat = scanner.nextLine();
        try {
            performTimedSearch(() -> {
                try {
                    return productRepository.findByCategory(cat);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            System.err.println("Error searching by category: " + e.getMessage());
        }
    }

    private void searchByBrand() {
        System.out.print("Brand: ");
        String brand = scanner.nextLine();
        try {
            performTimedSearch(() -> {
                try {
                    return productRepository.findByBrand(brand);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            System.err.println("Error searching by brand: " + e.getMessage());
        }
    }

    private void searchByPrice() {
        System.out.print("Min price: ");
        double min = getDoubleInput();
        System.out.print("Max price: ");
        double max = getDoubleInput();
        try {
            performTimedSearch(() -> {
                try {
                    return productRepository.findByPriceRange(min, max);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            System.err.println("Error searching by price range: " + e.getMessage());
        }
    }

    private void performTimedSearch(SearchSupplier supplier) {
        try {
            long start = System.nanoTime();
            List<Product> results = supplier.get();
            long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            if (results.isEmpty()) {
                System.out.println("No results found.");
            } else {
                results.forEach(System.out::println);
            }
            System.out.println("(Query executed in " + timeMs + " ms)");
        } catch (Exception e) {
            System.err.println("Error performing search: " + e.getMessage());
        }
    }

    private void updateProduct() {
        System.out.print("Product ID: ");
        long id = getLongInput();
        try {
            Product p = productRepository.findById(id);
            if (p == null) {
                System.out.println("Product not found.");
                return;
            }
            System.out.println("Current data: " + p);
            System.out.print("New name: ");
            String name = scanner.nextLine();
            System.out.print("New category: ");
            String category = scanner.nextLine();
            System.out.print("New brand: ");
            String brand = scanner.nextLine();
            System.out.print("New price: ");
            double price = getDoubleInput();
            System.out.print("New description: ");
            String desc = scanner.nextLine();

            boolean updated = productRepository.update(id, name, category, brand, price, desc);
            if (updated) {
                System.out.println("Product updated successfully.");
            } else {
                System.out.println("Failed to update product.");
            }
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
    }


    private void deleteProduct() {
        System.out.print("Product ID: ");
        long id = getLongInput();
        try {
            boolean deleted = productRepository.deleteById(id);
            if (deleted) {
                System.out.println("Product deleted.");
            } else {
                System.out.println("Product not found.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
    }

    private long getLongInput() {
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Using 0.");
            return 0L;
        }
    }

    private void showAuditLogs() {
        try {
            auditService.printAllLogs();
        } catch (Exception e) {
            System.err.println("Error showing audit logs: " + e.getMessage());
        }
    }

    private void showMetrics() {
        try {
            int totalProducts = productRepository.size();
            System.out.println("Total products: " + totalProducts);
        } catch (Exception e) {
            System.err.println("Error getting metrics: " + e.getMessage());
        }
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Using -1.");
            return -1;
        }
    }

    private double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Using 0.");
            return 0.0;
        }
    }

    @FunctionalInterface
    private interface SearchSupplier {
        List<Product> get();
    }
}