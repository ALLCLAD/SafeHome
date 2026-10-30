package com.safehome.dto.routine;

import com.safehome.model.enums.PresetScenario;

import java.util.List;

/**
 * DTO record détaillant le compte-rendu de l'exécution d'un scénario.
 *
 * @param scenario Le scénario exécuté.
 * @param message Message synthétique de résultat.
 * @param actionsApplied Liste détaillée des actions exécutées sur les équipements.
 */
public record ScenarioExecutionResult(
        PresetScenario scenario,
        String message,
        List<String> actionsApplied
) {
}
