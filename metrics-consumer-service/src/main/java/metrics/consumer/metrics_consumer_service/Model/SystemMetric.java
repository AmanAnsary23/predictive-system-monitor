package metrics.consumer.metrics_consumer_service.Model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SystemMetric {
    private String serviceName;
    private double cpuUsage;
    private int dbConnections;
    private Long responseTimeMs;
    private LocalDateTime timestamp;
}
