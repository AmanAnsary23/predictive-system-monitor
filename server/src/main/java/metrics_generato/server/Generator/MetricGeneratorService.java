package metrics_generato.server.Generator;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import metrics_generato.server.Model.SystemMetric;
import metrics_generato.server.Producer.MetricProducerService;

@Service
public class MetricGeneratorService {

    @Autowired
    private MetricProducerService producerService;

    private final Random random = new Random();

    @Scheduled(fixedRate = 5000)
    public void generateMetric() {
        SystemMetric metric = new SystemMetric();
        String[] services = { "order-service", "payment-service", "inventory-service" };
        metric.setServiceName(services[random.nextInt(services.length)]);
        metric.setCpuUsage(20 + random.nextDouble() * 60);
        metric.setDbConnections(random.nextInt(100));
        metric.setResponseTimeMs(50 + random.nextInt(200));
        metric.setTimestamp(LocalDateTime.now());

        producerService.sendMetric(metric);

        System.out.println(metric.getServiceName() + " | CPU: " + Math.round(metric.getCpuUsage() * 100.0) / 100.0
        + " | DB Conn: " + metric.getDbConnections()
        + " | Response: " + metric.getResponseTimeMs() + "ms");

        
    }
}
