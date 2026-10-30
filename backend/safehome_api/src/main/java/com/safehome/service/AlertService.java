package com.safehome.service;

import com.safehome.dto.alert.AlertResponse;
import com.safehome.dto.alert.SimulateIncidentRequest;
import com.safehome.model.enums.AlertSeverity;
import com.safehome.model.enums.ZoneType;

import java.util.List;

/**
 * Contrat de service pour la gestion des alertes et incidents en temps réel (Écran 2).
 * Respecte le principe d'Inversion des Dépendances (DIP) de SOLID.
 */
public interface AlertService {

    /**
     * Récupère l'ensemble des alertes par ordre antéchronologique (les plus récentes en premier).
     *
     * @return Liste ordonnée de AlertResponse.
     */
    List<AlertResponse> getAllAlerts();

    /**
     * Récupère uniquement les alertes actives non résolues.
     *
     * @return Liste des alertes non résolues.
     */
    List<AlertResponse> getActiveAlerts();

    /**
     * Récupère les alertes filtrées par zone.
     *
     * @param zone La pièce ciblée.
     * @return Liste d'alertes de la zone.
     */
    List<AlertResponse> getAlertsByZone(ZoneType zone);

    /**
     * Récupère les alertes filtrées par niveau de gravité.
     *
     * @param severity Le degré de sévérité (INFO, WARNING, CRITICAL).
     * @return Liste d'alertes correspondantes.
     */
    List<AlertResponse> getAlertsBySeverity(AlertSeverity severity);

    /**
     * Simule un incident domestique (Mode Démo : Fuite d'eau, Fumée, Intrusion).
     * Enregistre l'alerte en BDD, publie l'état sur MQTT et trace l'événement dans l'audit.
     *
     * @param request Paramètres de simulation d'incident.
     * @param userEmail Email de l'utilisateur ayant déclenché la simulation.
     * @return L'alerte créée.
     */
    AlertResponse simulateIncident(SimulateIncidentRequest request, String userEmail);

    /**
     * Déclenche la procédure d'urgence générale (Bouton d'urgence - Écran 2).
     * Verrouille l'ensemble des portes et garages, génère une alerte CRITICAL,
     * notifie le broker MQTT et consigne l'action dans l'audit.
     *
     * @param userEmail Email de l'utilisateur activant l'urgence.
     * @return L'alerte d'urgence générée.
     */
    AlertResponse triggerEmergency(String userEmail);

    /**
     * Acquitte ou marque une alerte comme résolue.
     *
     * @param id Identifiant de l'alerte à résoudre.
     * @param userEmail Email de l'utilisateur acquittant l'alerte.
     * @return L'alerte mise à jour.
     */
    AlertResponse resolveAlert(Long id, String userEmail);
}
