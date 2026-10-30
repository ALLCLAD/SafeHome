package com.safehome.dto.alert;

import com.safehome.model.Alert;
import com.safehome.model.enums.AlertSeverity;
import com.safehome.model.enums.ZoneType;

import java.time.LocalDateTime;

/**
 * DTO record représentant une alerte de sécurité ou incident domestique (Écran 2).
 * Implémenté sous forme de record Java pour garantir l'immutabilité des données d'incident.
 *
 * @param id L'identifiant unique de l'alerte en base de données.
 * @param zone La pièce ou zone concernée par l'alerte (SALON, CUISINE, etc.).
 * @param severity Le niveau de gravité (INFO, WARNING, CRITICAL).
 * @param message Le message descriptif de l'événement (ex: "Fumée détectée !").
 * @param timestamp L'horodatage précis du déclenchement.
 * @param isResolved Indique si l'incident a été acquitté/résolu par l'utilisateur.
 */
public record AlertResponse(
        // Identifiant primaire de l'alerte
        Long id,
        // Zone où s'est produit l'incident
        ZoneType zone,
        // Sévérité de l'alerte
        AlertSeverity severity,
        // Libellé explicatif de l'anomalie
        String message,
        // Horodatage de génération
        LocalDateTime timestamp,
        // Statut de résolution
        Boolean isResolved
) {
    /**
     * Méthode utilitaire de fabrique pour convertir une entité JPA Alert vers ce record.
     *
     * @param alert L'entité Alert issue de la base de données.
     * @return L'instance immuable de AlertResponse correspondante.
     */
    public static AlertResponse fromEntity(Alert alert) {
        // Construction du record avec les attributs de l'entité
        return new AlertResponse(
                alert.getId(),
                alert.getZone(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getTimestamp(),
                alert.getIsResolved()
        );
    }
}
