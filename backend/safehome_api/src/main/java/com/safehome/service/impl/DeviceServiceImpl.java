package com.safehome.service.impl;

import com.safehome.dto.device.DeviceCommandRequest;
import com.safehome.dto.device.DeviceResponse;
import com.safehome.model.Device;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import com.safehome.repository.DeviceRepository;
import com.safehome.service.AuditService;
import com.safehome.service.DeviceService;
import com.safehome.service.MqttGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implémentation du service DeviceService.
 * Gère le cycle de vie et l'état en BDD des appareils, coordonne la publication
 * des commandes via MQTT et journalise chaque interaction dans l'AuditLog.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    // Repository Spring Data pour la table devices
    private final DeviceRepository deviceRepository;

    // Passerelle MQTT dédiée aux échanges réseau avec le broker
    private final MqttGatewayService mqttGatewayService;

    // Service d'audit traçant les ordres de commande émis par les utilisateurs
    private final AuditService auditService;

    /**
     * Retourne la totalité des appareils du domicile.
     *
     * @return Liste de DeviceResponse immuables.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        // Recherche tous les équipements et les convertit en DTO record
        return deviceRepository.findAll().stream()
                .map(DeviceResponse::fromEntity)
                .toList();
    }

    /**
     * Filtre les équipements par zone (SALON, CUISINE, etc.).
     *
     * @param zone La zone choisie.
     * @return Liste de DeviceResponse de cette zone.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByZone(ZoneType zone) {
        // Requête par zone puis conversion en DTO record
        return deviceRepository.findByZone(zone).stream()
                .map(DeviceResponse::fromEntity)
                .toList();
    }

    /**
     * Filtre les équipements par type matériel (LIGHT, PIR, etc.).
     *
     * @param type Le type sélectionné.
     * @return Liste de DeviceResponse correspondants.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByType(DeviceType type) {
        // Requête par type puis conversion en DTO record
        return deviceRepository.findByType(type).stream()
                .map(DeviceResponse::fromEntity)
                .toList();
    }

    /**
     * Récupère un appareil spécifique par son identifiant unique.
     *
     * @param id L'identifiant de l'appareil.
     * @return Le DeviceResponse trouvé.
     */
    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        // Recherche en base avec exception si non trouvé
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Équipement non trouvé avec l'id : " + id));

        // Conversion en DTO record
        return DeviceResponse.fromEntity(device);
    }

    /**
     * Reçoit un ordre de commande depuis l'application mobile Flutter, met à jour
     * la base de données, transmet la commande MQTT et enregistre une ligne d'audit.
     *
     * @param id L'identifiant de l'appareil commandé.
     * @param request Le payload de la commande (ex: "{\"state\":\"ON\"}").
     * @param userEmail L'email de l'utilisateur authentifié ayant déclenché l'ordre.
     * @return Le DeviceResponse actualisé.
     */
    @Override
    @Transactional
    public DeviceResponse sendCommand(Long id, DeviceCommandRequest request, String userEmail) {
        // Récupération de l'appareil en BDD
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Équipement introuvable avec l'id : " + id));

        // Mise à jour de l'état en base de données avec le payload de la commande
        device.setStateValue(request.command());
        Device updatedDevice = deviceRepository.save(device);

        // Publication de la commande sur le topic MQTT approprié
        mqttGatewayService.publishCommand(device.getZone(), device.getType(), request.command());

        // Enregistrement de l'action dans le journal d'audit (Écran 4)
        String actionName = "COMMAND_" + device.getType().name();
        auditService.log(userEmail, actionName, device.getName() + " (" + device.getZone().name() + ")");

        // Retour du DTO record à jour
        return DeviceResponse.fromEntity(updatedDevice);
    }

    /**
     * Répercute en BDD une modification d'état reçue en écoute depuis le broker MQTT (status topic).
     *
     * @param zone La zone de l'appareil.
     * @param type Le type de l'appareil.
     * @param statePayload Le nouvel état reçu.
     */
    @Override
    @Transactional
    public void updateDeviceStatus(ZoneType zone, DeviceType type, String statePayload) {
        // Recherche des équipements correspondant à cette zone et ce type
        List<Device> devices = deviceRepository.findByZoneAndType(zone, type);

        // Mise à jour de chacun des appareils trouvés
        for (Device device : devices) {
            // Actualisation du statut sérialisé
            device.setStateValue(statePayload);
            // Indique que l'appareil est actif et en ligne
            device.setIsOnline(true);
            // Sauvegarde des modifications
            deviceRepository.save(device);

            log.info("Mise à jour état reçu MQTT pour '{}' ({}) : {}", device.getName(), zone, statePayload);
        }
    }
}
