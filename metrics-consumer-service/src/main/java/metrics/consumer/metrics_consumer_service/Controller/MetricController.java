package metrics.consumer.metrics_consumer_service.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import metrics.consumer.metrics_consumer_service.Entity.SystemMetricEntity;
import metrics.consumer.metrics_consumer_service.Repository.SystemMetricRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/metrics")
public class MetricController {
    
    @Autowired
    private SystemMetricRepository repository;

    @GetMapping("/latest")
    public List<SystemMetricEntity> getLatestMetrics() {
        return repository.findTop20ByOrderByIdDesc();
    }
}
