package metrics_generato.server.Producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import metrics_generato.server.Model.SystemMetric;

@Service
public class MetricProducerService {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    private static final String TOPIC = "system-metrics";

    public void sendMetric(SystemMetric metric) {
        kafkaTemplate.send(TOPIC, metric.getServiceName(), metric);
    }
}
