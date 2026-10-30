package com.safehome.dto.device;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO record pour ordonner une action ou un changement d'état sur un équipement (Écran 1).
 *
 * @param command Le payload ou instruction à envoyer (ex: "{\"state\":\"ON\"}" ou "ON").
 */
public record DeviceCommandRequest(
        // Commande obligatoire à transmettre à l'équipement
        @NotBlank(message = "La commande d'action est requise")
        String command
) {
}
