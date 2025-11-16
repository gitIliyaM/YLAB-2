package y_lab.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class Config {
    private final Map<String, Object> configMap;

    public Config(String yamlFile) {
        Yaml yaml = new Yaml();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(yamlFile)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + yamlFile);
            }

            this.configMap = yaml.load(input);
            if (this.configMap == null) {
                throw new RuntimeException("Configuration file " + yamlFile + " is empty or invalid.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading configuration from " + yamlFile, e);
        }
    }

    private Object getNestedValue(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> currentMap = configMap;
        for (int i = 0; i < parts.length - 1; i++) {
            Object value = currentMap.get(parts[i]);
            if (value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else {
                return null;
            }
        }
        return currentMap.get(parts[parts.length - 1]);
    }

    private String getString(String key) {
        Object value = getNestedValue(key);
        return value != null ? value.toString() : null;
    }

    private int getInt(String key, int defaultValue) {
        Object value = getNestedValue(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse integer for key '" + key + "', using default: " + defaultValue);
                return defaultValue;
            }
        }
        System.err.println("Key '" + key + "' not found or not a number, using default: " + defaultValue);
        return defaultValue;
    }

    private long getLong(String key, long defaultValue) {
        Object value = getNestedValue(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse long for key '" + key + "', using default: " + defaultValue);
                return defaultValue;
            }
        }
        System.err.println("Key '" + key + "' not found or not a number, using default: " + defaultValue);
        return defaultValue;
    }


    public HikariDataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getString("db.url"));
        config.setUsername(getString("db.username"));
        config.setPassword(getString("db.password"));
        config.setDriverClassName(getString("db.driver"));

        config.setMaximumPoolSize(getInt("db.pool.maxSize", 10));
        config.setMinimumIdle(getInt("db.pool.minIdle", 2));
        config.setConnectionTimeout(getLong("db.pool.connectionTimeout", 30000));

        return new HikariDataSource(config);
    }

    public String getLiquibaseChangelogFile() {
        return getString("liquibase.changelog-file");
    }

    public String getLiquibaseSchema() {
        return getString("liquibase.ChangeLogTableSchema");
    }
}