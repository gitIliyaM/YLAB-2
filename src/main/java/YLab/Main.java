package YLab;

import YLab.repository.ProductRepository;
import YLab.service.AuditService;
import YLab.service.DataService;
import YLab.service.ProductService;

public class Main {
    public static void main(String[] args) {
        DataService persistence = new DataService();
        ProductRepository productRepository = new ProductRepository();
        productRepository.setProducts(persistence.loadProducts());

        AuditService auditService = new AuditService();
        auditService.setLogs(persistence.loadAudit());

        ProductService productService = new ProductService(productRepository, auditService);
        ConsoleUI ui = new ConsoleUI(productService, auditService, persistence);

        ui.start();

        persistence.saveProducts(productService.getAllProducts());
        persistence.saveAudit(auditService.getLogs());
        System.out.println("Data saved. Program terminated.");
    }
}