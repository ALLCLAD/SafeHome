package com.safehome.service.impl;

import com.safehome.dto.alert.AlertResponse;
import com.safehome.dto.alert.SimulateIncidentRequest;
import com.safehome.model.Alert;
import com.safehome.model.Device;
import com.safehome.model.enums.AlertSeverity;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import com.safehome.repository.AlertRepository;
import com.safehome.repository.DeviceRepository;
import com.safehome.service.AlertService;
import com.safehome.service.AuditService;
import com.safehome.service.MqttGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implémentation du service AlertService.
 * Gère la persistance de l'historique chronologique des alertes,
 * la simulation d'incidents (Mode Démo) et la procédure de verrouillage d'urgence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    // Repository pour l'accès aux alertes
    private final AlertRepository alertRepository;

    // Repository pour manipuler l'état des équipements lors d'urgences
    private final DeviceRepository deviceRepository;

    // Passerelle de publication des messages vers le broker MQTT
    private final MqttGatewayService mqttGatewayService;

    // Service d'audit pour enregistrer chaque action de sécurité
    private final AuditService auditService;

    /**
     * Retourne l'historique complet de toutes les alertes par ordre chronologique inversé.
     *
     * @return Liste ordonnée de AlertResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlerts() {
        // Récupération de toutes les alertes triées par timestamp décroissant
        return alertRepository.findAllByOrderByTimestampDesc().stream()
                // Conversion de chaque entité Alert en DTO record
                .map(AlertResponse::fromEntity)
                // Collecte en liste immuable
                .toList();
    }

    /**
     * Retourne uniquement les alertes qui ne sont pas encore résolues.
     *
     * @return Liste ordonnée des alertes actives.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlerts() {
        // Récupération des alertes non résolues
        return alertRepository.findByIsResolvedFalseOrderByTimestampDesc().stream()
                // Conversion en DTO record
                .map(AlertResponse::fromEntity)
                // Collecte en liste
                .toList();
    }

    /**
     * Retourne les alertes d'une zone spécifique.
     *
     * @param zone La zone ciblée.
     * @return Liste ordonnée d'alertes.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByZone(ZoneType zone) {
        // Filtrage par zone
        return alertRepository.findByZoneOrderByTimestampDesc(zone).stream()
                // Conversion en DTO record
                .map(AlertResponse::fromEntity)
                // Collecte en liste
                .toList();
    }

    /**
     * Retourne les alertes selon leur niveau de gravité.
     *
     * @param severity Le degré de sévérité.
     * @return Liste ordonnée d'alertes.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsBySeverity(AlertSeverity severity) {
        // Filtrage par niveau de criticité
        return alertRepository.findBySeverityOrderByTimestampDesc(severity).stream()
                // Conversion en DTO record
                .map(AlertResponse::fromEntity)
                // Collecte en liste
                .toList();
    }

    /**
     * Simule un incident domestique (Fumée, Fuite d'eau, Intrusion PIR).
     * Enregistre l'alerte, notifie via MQTT et consigne dans l'audit.
     *
     * @param request Paramètres de simulation d'incident.
     * @param userEmail Email de l'utilisateur ayant déclenché la simulation.
     * @return L'alerte créée.
     */
    @Override
    @Transactional
    public AlertResponse simulateIncident(SimulateIncidentRequest request, String userEmail) {
        // Détermination de la sévérité (CRITICAL par défaut si non spécifiée)
        AlertSeverity severity = request.severity() != null ? request.severity() : AlertSeverity.CRITICAL;

        // Construction et persistance de la nouvelle alerte en BDD PostgreSQL
        Alert alert = Alert.builder()
                .zone(request.zone())
                .severity(severity)
                .message(request.message())
                .isResolved(false)
                .build();
        Alert savedAlert = alertRepository.save(alert);

        // Détection du capteur impliqué pour publier l'événement sur le bon topic de simulation MQTT
        DeviceType targetSensor = resolveSensorFromMessage(request.message());
        if (targetSensor != null) {
            String topicPayload = String.format("{\"alert\": true, \"message\": \"%s\", \"severity\": \"%s\"}",
                    request.message(), severity.name());
            mqttGatewayService.publishCommand(request.zone(), targetSensor, topicPayload);
        }

        // Journalisation de la simulation dans l'AuditLog
        auditService.log(userEmail, "SIMULATION_INCIDENT", request.zone().name() + " : " + request.message());

        log.info("Incident simulé enregistré : [{}] {} - {}", severity, request.zone(), request.message());

        // Retour du DTO record
        return AlertResponse.fromEntity(savedAlert);
    }

    /**
     * Procédure d'urgence générale (Bouton d'urgence - Écran 2) :
     * Verrouille toutes les serrures / portes de garage, génère une alerte CRITICAL
     * et consigne l'intervention dans l'audit.
     *
     * @param userEmail Email de l'utilisateur initiateur.
     * @return L'alerte d'urgence générée.
     */
    @Override
    @Transactional
    public AlertResponse triggerEmergency(String userEmail) {
        // 1. Verrouillage de tous les verrous et portes de garage en base de données
        List<Device> locks = deviceRepository.findByType(DeviceType.LOCK);
        for (Device lock : locks) {
            lock.setStateValue("{\"state\": \"LOCKED\"}");
            deviceRepository.save(lock);
            // Envoi de l'ordre MQTT de verrouillage
            mqttGatewayService.publishCommand(lock.getZone(), DeviceType.LOCK, "{\"state\": \"LOCKED\"}");
        }

        // 2. Publication d'un message d'alarme générale sur MQTT
        mqttGatewayService.publish("safehome_charo_x789/home/security/alarm", "{\"alarm\": \"TRIGGERED\", \"mode\": \"EMERGENCY\"}");

        // 3. Enregistrement de l'alerte critique en BDD
        Alert emergencyAlert = Alert.builder()
                .zone(ZoneType.SALON)
                .severity(AlertSeverity.CRITICAL)
                .message("PROCÉDURE D'URGENCE DÉCLENCHÉE : Verrouillage général & Sirène activée !")
                .isResolved(false)
                .build();
        Alert savedAlert = alertRepository.save(emergencyAlert);

        // 4. Inscription dans le journal d'audit
        auditService.log(userEmail, "BOUTON_URGENCE_DECLENCHE", "SYSTEME_COMPLET");

        log.warn("URGENCE GLOBALE ACTIVÉE PAR : {}", userEmail);

        // Retour du DTO record
        return AlertResponse.fromEntity(savedAlert);
    }

    /**
     * Marque une alerte existante comme résolue.
     *
     * @param id Identifiant de l'alerte.
     * @param userEmail Email de l'utilisateur qui acquitte.
     * @return L'alerte mise à jour.
     */
    @Override
    @Transactional
    public AlertResponse resolveAlert(Long id, String userEmail) {
        // Recherche de l'alerte en BDD
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerte introuvable avec l'identifiant : " + id));

        // Mise à jour de l'état de résolution
        alert.setIsResolved(true);
        Alert updatedAlert = alertRepository.save(alert);

        // Inscription dans l'audit
        auditService.log(userEmail, "ALERTE_RESOLUE", "Alerte #" + id + " (" + alert.getMessage() + ")");

        log.info("Alerte #{} résolue par {}", id, userEmail);

        // Retour du DTO record
        return AlertResponse.fromEntity(updatedAlert);
    }

    /**
     * Déduit le type de capteur en fonction des mots-clés présents dans le message de simulation.
     */
    private DeviceType resolveSensorFromMessage(String message) {
        String msgLower = message.toLowerCase();
        if (msgLower.contains("fumée") || msgLower.contains("smoke") || msgLower.contains("incendie")) {
            return DeviceType.SMOKE_SENSOR;
        } else if (msgLower.contains("eau") || msgLower.contains("water") || msgLower.contains("fuite")) {
            return DeviceType.WATER_LEAK;
        } else if (msgLower.contains("mouvement") || msgLower.contains("motion") || msgLower.contains("intrusion")) {
            return DeviceType.PIR;
        }
        return null;
    }
}
