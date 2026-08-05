package metrics.consumer.metrics_consumer_service.Service;

import org.springframework.stereotype.Service;

import metrics.consumer.metrics_consumer_service.Model.SystemMetric;

@Service
public class AnomalyDetectionService {
    
    public boolean isAnomalous(SystemMetric metric) {
        if(metric.getCpuUsage() > 85) return true;
        if(metric.getDbConnections() > 90) return true;
        if(metric.getResponseTimeMs() > 250) return true;
        return false; 
    }
}
