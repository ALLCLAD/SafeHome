package com.safehome.dto.routine;

import com.safehome.model.enums.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO record pour créer ou mettre à jour une routine programmée.
 *
 * @param name Nom de la routine (ex: "Fermeture automatique garage").
 * @param triggerType Type de déclencheur (TIME ou EVENT).
 * @param cronExpression Cron temporelle si TIME.
 * @param conditionExpr Condition d'événement si EVENT.
 * @param actionPayload Actions à exécuter au format texte ou JSON.
 * @param isActive Statut actif ou inactif.
 */
public record RoutineRequest(
        @NotBlank(message = "Le nom de la routine est requis")
        String name,

        @NotNull(message = "Le type de déclencheur est requis")
        TriggerType triggerType,

        String cronExpression,

        String conditionExpr,

        @NotBlank(message = "Le payload d'action est requis")
        String actionPayload,

        Boolean isActive
) {
}
