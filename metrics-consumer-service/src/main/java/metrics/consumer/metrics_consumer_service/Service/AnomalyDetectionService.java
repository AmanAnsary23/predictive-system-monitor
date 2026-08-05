package metrics.consumer.metrics_consumer_service.Service;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import metrics.consumer.metrics_consumer_service.MetricHistoryTracker;
import metrics.consumer.metrics_consumer_service.Model.SystemMetric;

@Service
public class AnomalyDetectionService {

    @Autowired
    private MetricHistoryTracker historyTracker;

    public boolean isAnomalous(SystemMetric metric) {

        String serviceName = metric.getServiceName();
        LinkedList<Double> history = historyTracker.getHistory(serviceName);

        if (history.size() < 5) {
            historyTracker.addReading(serviceName, metric.getCpuUsage());
            return false;
        }

        double mean = history.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = history.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        double currentCpu = metric.getCpuUsage();
        boolean isStatisticalAnomaly = Math.abs(currentCpu - mean) > (2 * stdDev) && stdDev > 0;

        historyTracker.addReading(serviceName, currentCpu);

        boolean isHardLimitBreach = metric.getDbConnections() > 90 || metric.getResponseTimeMs() > 250;

        return isStatisticalAnomaly || isHardLimitBreach;
    }
}
