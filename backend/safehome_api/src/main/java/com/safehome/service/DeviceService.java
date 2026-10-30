package com.safehome.service;

import com.safehome.dto.device.DeviceCommandRequest;
import com.safehome.dto.device.DeviceResponse;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;

import java.util.List;

/**
 * Contrat de service pour la gestion des équipements et capteurs de la maison (Écran 1).
 * Respecte SOLID en déléguant la persistance au DeviceRepository et la transmission IoT au MqttGatewayService.
 */
public interface DeviceService {

    /**
     * Récupère la liste de tous les équipements enregistrés.
     *
     * @return Liste de DeviceResponse.
     */
    List<DeviceResponse> getAllDevices();

    /**
     * Récupère la liste des équipements situés dans une pièce ou zone donnée.
     *
     * @param zone La zone ciblée (SALON, CUISINE, CHAMBRE, GARAGE).
     * @return Liste de DeviceResponse filtrée.
     */
    List<DeviceResponse> getDevicesByZone(ZoneType zone);

    /**
     * Récupère la liste des équipements selon leur type matériel.
     *
     * @param type Le type d'appareil (LIGHT, LOCK, etc.).
     * @return Liste de DeviceResponse filtrée.
     */
    List<DeviceResponse> getDevicesByType(DeviceType type);

    /**
     * Récupère un équipement par son identifiant unique.
     *
     * @param id L'identifiant de l'équipement.
     * @return Le DeviceResponse trouvé.
     */
    DeviceResponse getDeviceById(Long id);

    /**
     * Exécute une commande de contrôle sur un équipement actionneur.
     * Met à jour l'état en base, publie la commande via MQTT et enregistre l'action dans l'audit.
     *
     * @param id L'identifiant de l'appareil à piloter.
     * @param request La commande à transmettre.
     * @param userEmail L'email de l'utilisateur qui a déclenché la commande.
     * @return L'état mis à jour de l'appareil.
     */
    DeviceResponse sendCommand(Long id, DeviceCommandRequest request, String userEmail);

    /**
     * Met à jour le statut réel d'un équipement suite à un retour MQTT (status topic).
     *
     * @param zone La zone de l'appareil.
     * @param type Le type de l'appareil.
     * @param statePayload Le nouvel état reçu.
     */
    void updateDeviceStatus(ZoneType zone, DeviceType type, String statePayload);
}
