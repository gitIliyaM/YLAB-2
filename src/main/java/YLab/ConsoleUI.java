package YLab;

import YLab.model.Product;
import YLab.service.AuditService;
import YLab.service.DataService;
import YLab.service.ProductService;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConsoleUI {
    private static final String ADMIN_EMAIL = "user@ru";
    private static final String ADMIN_PASSWORD = "123";
    private boolean exitRequested = false;
    private final Scanner scanner = new Scanner(System.in);
    private final ProductService productService;
    private final AuditService auditService;
    private final DataService dataService;
    private boolean loggedIn = false;

    public ConsoleUI(
            ProductService productService,
            AuditService auditService,
            DataService dataService
    ) {
        this.productService = productService;
        this.auditService = auditService;
        this.dataService = dataService;
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
            auditService.log("LOGIN", "User: " + email);
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
            case 8 -> auditService.printAllLogs();
            case 9 -> showMetrics();
            case 0 -> {
                auditService.log("LOGOUT", "User: " + ADMIN_EMAIL);
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

        String id = productService.addProduct(name, category, brand, price, desc);
        System.out.println("Product added with ID: " + id);
    }

    private void listAll() {
        long start = System.nanoTime();
        List<Product> products = productService.getAllProducts();
        long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (products.isEmpty()) {
            System.out.println("Catalog is empty.");
        } else {
            products.forEach(System.out::println);
        }
        System.out.println("(Query executed in " + timeMs + " ms)");
    }

    private void searchByCategory() {
        System.out.print("Category: ");
        String cat = scanner.nextLine();
        performTimedSearch(() -> productService.searchByCategory(cat));
    }

    private void searchByBrand() {
        System.out.print("Brand: ");
        String brand = scanner.nextLine();
        performTimedSearch(() -> productService.searchByBrand(brand));
    }

    private void searchByPrice() {
        System.out.print("Min price: ");
        double min = getDoubleInput();
        System.out.print("Max price: ");
        double max = getDoubleInput();
        performTimedSearch(() -> productService.searchByPriceRange(min, max));
    }

    private void performTimedSearch(SearchSupplier supplier) {
        long start = System.nanoTime();
        List<Product> results = supplier.get();
        long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            results.forEach(System.out::println);
        }
        System.out.println("(Query executed in " + timeMs + " ms)");
    }

    private void updateProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine();
        Product p = productService.findById(id);
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

        if (productService.updateProduct(id, name, category, brand, price, desc)) {
            System.out.println("Product updated successfully.");
        } else {
            System.out.println("Failed to update product.");
        }
    }

    private void deleteProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine();
        if (productService.deleteProduct(id)) {
            System.out.println("Product deleted.");
        } else {
            System.out.println("Product not found.");
        }
    }

    private void showMetrics() {
        System.out.println("Total products: " + productService.getTotalProducts());
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
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