package com.safehome.model;

import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ZoneType zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType type;

    /**
     * Représentation sous forme de String ou JSON de l'état actuel :
     * Exemples :
     * - Pour une lampe : "{\"state\": \"ON\", \"brightness\": 80}"
     * - Pour une porte / serrure : "{\"state\": \"LOCKED\"}" ou "{\"state\": \"OPEN\"}"
     * - Pour un thermostat : "{\"target_temp\": 22.5, \"current_temp\": 21.0}"
     * - Pour un capteur PIR : "{\"motion\": false}"
     */
    @Column(name = "state_value", columnDefinition = "TEXT")
    private String stateValue;

    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = true;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
