package com.safehome.repository;

import com.safehome.model.Routine;
import com.safehome.model.enums.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findByIsActiveTrue();
    List<Routine> findByTriggerTypeAndIsActiveTrue(TriggerType triggerType);
}
