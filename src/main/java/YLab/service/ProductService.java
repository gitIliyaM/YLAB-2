package YLab.service;

import YLab.model.Product;
import YLab.repository.ProductRepository;
import java.util.List;
import java.util.UUID;

public class ProductService {
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, AuditService auditService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public String addProduct(String name, String category, String brand, double price, String description) {
        String id = UUID.randomUUID().toString();
        Product p = new Product(id, name, category, brand, price, description);
        productRepository.save(p);
        auditService.log("ADD_PRODUCT", "Product ID: " + id + ", Name: " + name);
        return id;
    }

    public boolean updateProduct(String id, String name, String category, String brand, double price, String description) {
        Product existing = productRepository.findById(id);
        if (existing == null) return false;
        Product updated = new Product(id, name, category, brand, price, description);
        productRepository.update(updated);
        auditService.log("UPDATE_PRODUCT", "Product ID: " + id);
        return true;
    }

    public boolean deleteProduct(String id) {
        boolean removed = productRepository.deleteById(id);
        if (removed) {
            auditService.log("DELETE_PRODUCT", "Product ID: " + id);
        }
        return removed;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> searchByPriceRange(double min, double max) {
        return productRepository.findByPriceRange(min, max);
    }

    public Product findById(String id) {
        return productRepository.findById(id);
    }

    public int getTotalProducts() {
        return productRepository.size();
    }
}