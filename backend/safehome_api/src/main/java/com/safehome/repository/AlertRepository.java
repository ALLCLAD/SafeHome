package com.safehome.repository;

import com.safehome.model.Alert;
import com.safehome.model.enums.AlertSeverity;
import com.safehome.model.enums.ZoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByOrderByTimestampDesc();
    List<Alert> findByIsResolvedFalseOrderByTimestampDesc();
    List<Alert> findByZoneOrderByTimestampDesc(ZoneType zone);
    List<Alert> findBySeverityOrderByTimestampDesc(AlertSeverity severity);
}
