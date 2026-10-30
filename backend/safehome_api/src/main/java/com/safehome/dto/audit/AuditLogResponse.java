package com.safehome.dto.audit;

import com.safehome.model.AuditLog;

import java.time.LocalDateTime;

/**
 * DTO record pour restituer les entrées de journal d'audit (Écran 4 du cahier des charges).
 * Répond au besoin de traçabilité : savoir quel utilisateur a agi sur quel équipement et à quel moment.
 *
 * @param id Identifiant de la trace d'audit.
 * @param userEmail Email de l'utilisateur (ou "SYSTEM" si déclencheur automatique).
 * @param action Libellé de l'action entreprise (ex: "COMMAND_LIGHT", "BOUTON_URGENCE_DECLENCHE").
 * @param targetDevice Équipement ou module ciblé.
 * @param timestamp Horodatage de l'événement.
 */
public record AuditLogResponse(
        Long id,
        String userEmail,
        String action,
        String targetDevice,
        LocalDateTime timestamp
) {
    /**
     * Convertit une entité JPA AuditLog en record AuditLogResponse.
     *
     * @param log L'entité AuditLog issue de la BDD.
     * @return L'instance immuable de AuditLogResponse.
     */
    public static AuditLogResponse fromEntity(AuditLog log) {
        String email = log.getUser() != null ? log.getUser().getEmail() : "SYSTEM";
        return new AuditLogResponse(
                log.getId(),
                email,
                log.getAction(),
                log.getTargetDevice(),
                log.getTimestamp()
        );
    }
}
