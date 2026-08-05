package metrics.consumer.metrics_consumer_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import metrics.consumer.metrics_consumer_service.Entity.SystemMetricEntity;

@Repository
public interface SystemMetricRepository extends JpaRepository<SystemMetricEntity , Long>{
    
    List<SystemMetricEntity> findTop20ByOrderByIdDesc();
}
