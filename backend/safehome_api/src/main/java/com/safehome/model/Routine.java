package com.safehome.model;

import com.safehome.model.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    /**
     * Expression cron pour triggerType = TIME (ex: "0 0 22 * * ?" pour tous les soirs à 22h)
     */
    @Column(name = "cron_expression")
    private String cronExpression;

    /**
     * Condition pour triggerType = EVENT (ex: "SENSOR_SMOKE == ALERT")
     */
    @Column(name = "condition_expr")
    private String conditionExpr;

    /**
     * Description ou payload JSON des actions à exécuter
     * Ex: "[{\"deviceId\": 2, \"action\": \"CLOSE\"}]" ou "LOCK_ALL"
     */
    @Column(name = "action_payload", columnDefinition = "TEXT", nullable = false)
    private String actionPayload;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
