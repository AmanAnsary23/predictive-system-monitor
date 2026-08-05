package metrics_generato.server.Model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SystemMetric {
    private String serviceName;
    private double cpuUsage;
    private int dbConnections;
    private long responseTimeMs;
    private LocalDateTime timestamp;
}
