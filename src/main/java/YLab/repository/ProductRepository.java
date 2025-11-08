package YLab.repository;

import YLab.model.Product;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProductRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<String, List<Product>> byCategory = new ConcurrentHashMap<>();
    private final Map<String, List<Product>> byBrand = new ConcurrentHashMap<>();

    public void save(Product p) {
        products.put(p.getId(), p);
        updateIndexes();
    }

    public Product findById(String id) {
        return products.get(id);
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public boolean deleteById(String id) {
        Product removed = products.remove(id);
        if (removed != null) {
            updateIndexes();
            return true;
        }
        return false;
    }

    public void update(Product p) {
        if (products.containsKey(p.getId())) {
            products.put(p.getId(), p);
            updateIndexes();
        }
    }

    private void updateIndexes() {
        byCategory.clear();
        byBrand.clear();
        for (Product p : products.values()) {
            byCategory.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
            byBrand.computeIfAbsent(p.getBrand(), k -> new ArrayList<>()).add(p);
        }
    }

    public List<Product> findByCategory(String category) {
        return byCategory.getOrDefault(category, Collections.emptyList());
    }

    public List<Product> findByBrand(String brand) {
        return byBrand.getOrDefault(brand, Collections.emptyList());
    }

    public List<Product> findByPriceRange(double min, double max) {
        return products.values().stream()
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .collect(ArrayList::new, (list, p) -> list.add(p), (l1, l2) -> l1.addAll(l2));
    }

    public int size() {
        return products.size();
    }

    public void setProducts(List<Product> productList) {
        this.products.clear();
        productList.forEach(p -> this.products.put(p.getId(), p));
        updateIndexes();
    }
}