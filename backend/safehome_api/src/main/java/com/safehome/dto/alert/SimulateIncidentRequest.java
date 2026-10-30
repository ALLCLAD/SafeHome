package com.safehome.dto.alert;

import com.safehome.model.enums.AlertSeverity;
import com.safehome.model.enums.ZoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO record pour simuler ou déclencher un incident domestique (Mode Démo / Écran 2).
 *
 * @param zone La zone dans laquelle l'incident survient (SALON, CUISINE, etc.).
 * @param severity Le degré de criticité (INFO, WARNING, CRITICAL).
 * @param message Description de l'événement simulé (ex: "Fumée dense détectée en cuisine").
 */
public record SimulateIncidentRequest(
        // Zone géographique de l'incident
        @NotNull(message = "La zone de l'incident est requise")
        ZoneType zone,

        // Gravité de l'incident (optionnelle, CRITICAL par défaut si absente)
        AlertSeverity severity,

        // Message descriptif de l'événement
        @NotBlank(message = "Le message d'incident est requis")
        String message
) {
}
