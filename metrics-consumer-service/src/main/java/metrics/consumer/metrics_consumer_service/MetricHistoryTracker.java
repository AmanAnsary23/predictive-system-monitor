package metrics.consumer.metrics_consumer_service;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class MetricHistoryTracker {
    
    private static final int WINDOW_SIZE = 20;

    private final Map<String , LinkedList<Double>> cpuHistory = new ConcurrentHashMap<>();


    public void addReading(String serviceName , double cpuUsage) {
        cpuHistory.putIfAbsent(serviceName, new LinkedList<>());
        LinkedList<Double> history = cpuHistory.get(serviceName);

        history.addLast(cpuUsage);
        if (history.size() > WINDOW_SIZE) {
            history.removeFirst();
        }
    }

    public LinkedList<Double> getHistory(String serviceName) {
        return cpuHistory.getOrDefault(serviceName, new LinkedList<>());
    }
}
