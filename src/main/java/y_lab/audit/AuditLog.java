package y_lab.audit;

public class AuditLog {
    private String action;
    private String details;
    private long timestamp;

    public AuditLog(String action, String details) {
        this.action = action;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public AuditLog(String action, String details, long timestamp) {
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getAction() { return action; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "[" + new java.util.Date(timestamp) + "] " + action + ": " + details;
    }
}