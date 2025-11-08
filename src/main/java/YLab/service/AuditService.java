package YLab.service;

import YLab.audit.AuditLog;
import java.util.ArrayList;
import java.util.List;

public class AuditService {
    private final List<AuditLog> logs = new ArrayList<>();

    public void log(String action, String details) {
        AuditLog log = new AuditLog(action, details);
        logs.add(log);
        System.out.println("Audit: " + log);
    }

    public void printAllLogs() {
        System.out.println("=== Audit ===");
        if (logs.isEmpty()) {
            System.out.println("There are no records");
        } else {
            logs.forEach(System.out::println);
        }
    }

    public List<AuditLog> getLogs() {
        return new ArrayList<>(logs);
    }

    public void setLogs(List<AuditLog> logs) {
        this.logs.clear();
        this.logs.addAll(logs);
    }
}