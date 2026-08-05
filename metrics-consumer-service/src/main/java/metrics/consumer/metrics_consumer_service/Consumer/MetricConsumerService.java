package metrics.consumer.metrics_consumer_service.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import metrics.consumer.metrics_consumer_service.Entity.SystemMetricEntity;
import metrics.consumer.metrics_consumer_service.Model.SystemMetric;
import metrics.consumer.metrics_consumer_service.Repository.SystemMetricRepository;
import metrics.consumer.metrics_consumer_service.Service.AnomalyDetectionService;

@Service
public class MetricConsumerService {

    @Autowired
    private SystemMetricRepository repository;

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @KafkaListener(topics = "system-metrics", groupId = "metrics-consumer-group")
    public void consumeMetric(SystemMetric metric) {

        SystemMetricEntity entity = new SystemMetricEntity();
        entity.setServiceName(metric.getServiceName());
        entity.setCpuUsage(metric.getCpuUsage());
        entity.setDbConnections(metric.getDbConnections());
        entity.setResponseTimeMs(metric.getResponseTimeMs());
        entity.setTimestamp(metric.getTimestamp());

        repository.save(entity);

        if (anomalyDetectionService.isAnomalous(metric)) {
            System.out.println("⚠️ ALERT: Anomaly detected in " + metric.getServiceName()
                    + " | CPU: " + metric.getCpuUsage()
                    + " | DB Conn: " + metric.getDbConnections()
                    + " | Response: " + metric.getResponseTimeMs() + "ms");
        }

        System.out.println("Received: " + metric.getServiceName()
                + " | CPU: " + metric.getCpuUsage()
                + " | DB Conn: " + metric.getDbConnections()
                + " | Response: " + metric.getResponseTimeMs() + "ms");
    }
}
