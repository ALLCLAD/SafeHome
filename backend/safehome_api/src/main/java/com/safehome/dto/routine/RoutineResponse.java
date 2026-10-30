package com.safehome.dto.routine;

import com.safehome.model.Routine;
import com.safehome.model.enums.TriggerType;

/**
 * DTO record représentant une routine ou automatisation (Écran 3).
 *
 * @param id Identifiant de la routine.
 * @param name Nom descriptif de la routine.
 * @param triggerType Type de déclencheur (TIME ou EVENT).
 * @param cronExpression Expression temporelle cron (si triggerType = TIME).
 * @param conditionExpr Condition logique (si triggerType = EVENT).
 * @param actionPayload Payload d'action à appliquer.
 * @param isActive Indique si la routine est active.
 */
public record RoutineResponse(
        Long id,
        String name,
        TriggerType triggerType,
        String cronExpression,
        String conditionExpr,
        String actionPayload,
        Boolean isActive
) {
    /**
     * Méthode utilitaire statique pour convertir une entité Routine JPA vers ce record.
     *
     * @param routine L'entité Routine.
     * @return L'instance immuable RoutineResponse.
     */
    public static RoutineResponse fromEntity(Routine routine) {
        return new RoutineResponse(
                routine.getId(),
                routine.getName(),
                routine.getTriggerType(),
                routine.getCronExpression(),
                routine.getConditionExpr(),
                routine.getActionPayload(),
                routine.getIsActive()
        );
    }
}
