package com.safehome.dto.device;

import com.safehome.model.Device;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;

import java.time.LocalDateTime;

/**
 * DTO record représentant un équipement renvoyé à l'application Flutter.
 * Fournit les informations complètes sur son type, sa localisation et son statut en direct.
 *
 * @param id L'identifiant unique de l'équipement en base de données.
 * @param name Le nom convivial de l'équipement (ex: "Lumière Principale").
 * @param zone La pièce ou zone associée (SALON, CUISINE, CHAMBRE, GARAGE).
 * @param type Le type matériel (LIGHT, LOCK, SHUTTER, THERMOSTAT, PIR, etc.).
 * @param stateValue L'état actuel au format JSON string (ex: "{\"state\":\"ON\"}").
 * @param isOnline Indique si l'équipement est joignable sur le réseau.
 * @param lastUpdated Horodatage de la dernière synchronisation d'état.
 */
public record DeviceResponse(
        // Identifiant primaire de l'équipement
        Long id,
        // Libellé de l'appareil
        String name,
        // Zone géographique de la maison
        ZoneType zone,
        // Type d'équipement (actionneur ou capteur)
        DeviceType type,
        // Valeur sérialisée de l'état
        String stateValue,
        // Indicateur de connexion active
        Boolean isOnline,
        // Date de dernière mise à jour
        LocalDateTime lastUpdated
) {
    /**
     * Méthode statique utilitaire permettant de mapper une entité Device JPA vers ce DTO record.
     *
     * @param device L'entité Device issue du repository.
     * @return L'instance immuable de DeviceResponse correspondante.
     */
    public static DeviceResponse fromEntity(Device device) {
        // Construction du record avec les données de l'entité
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getZone(),
                device.getType(),
                device.getStateValue(),
                device.getIsOnline(),
                device.getLastUpdated()
        );
    }
}
