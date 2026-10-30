package com.safehome.dto.routine;

import com.safehome.model.enums.PresetScenario;
import jakarta.validation.constraints.NotNull;

/**
 * DTO record pour activer un scénario prédéfini en 1 clic (Écran 3).
 *
 * @param scenario Le scénario choisi (DEPART_ABSENCE, NUIT, SECURITE_INCENDIE).
 */
public record TriggerScenarioRequest(
        @NotNull(message = "Le scénario à déclencher est requis")
        PresetScenario scenario
) {
}
