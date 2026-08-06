package metrics.consumer.metrics_consumer_service.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import metrics.consumer.metrics_consumer_service.MetricHistoryTracker;
import metrics.consumer.metrics_consumer_service.Model.SystemMetric;

@Service
public class AnomalyDetectionService {

    @Autowired
    private MetricHistoryTracker historyTracker;

    @Autowired
    private RestTemplate restTemplate;

    private static final String ML_API_URL = "http://localhost:5000/predict";

    public boolean isAnomalous(SystemMetric metric) {

        String serviceName = metric.getServiceName();
        LinkedList<Double> history = historyTracker.getHistory(serviceName);

        if (history.size() < 5) {
            historyTracker.addReading(serviceName, metric.getCpuUsage());
            return checkMlAndHardLimits(metric);
        }

        double mean = history.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = history.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        double currentCpu = metric.getCpuUsage();
        boolean isStatisticalAnomaly = Math.abs(currentCpu - mean) > (2 * stdDev) && stdDev > 0;

        historyTracker.addReading(serviceName, currentCpu);

        return isStatisticalAnomaly || checkMlAndHardLimits(metric);
    }

    private boolean checkMlAndHardLimits(SystemMetric metric) {
        boolean isHardLimitBreach = metric.getDbConnections() > 90 || metric.getResponseTimeMs() > 250;
        boolean isM1Anomaly = callMlService(metric);
        return isHardLimitBreach || isM1Anomaly;
    }

    private boolean callMlService(SystemMetric metric) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cpuUsage", metric.getCpuUsage());
            requestBody.put("dbConnections", metric.getDbConnections());
            requestBody.put("responseTimeMs", metric.getResponseTimeMs());

            Map response = restTemplate.postForObject(ML_API_URL, requestBody, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("isAnomaly"));
        } catch (Exception e) {
            System.out.println("ML service call failed: " + e.getMessage());
            return false; // fail-safe: if ML service is down, don't block detection
        }
    }
}
