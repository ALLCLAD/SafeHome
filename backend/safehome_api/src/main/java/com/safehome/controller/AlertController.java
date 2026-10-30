package com.safehome.controller;

import com.safehome.dto.alert.AlertResponse;
import com.safehome.dto.alert.SimulateIncidentRequest;
import com.safehome.model.enums.AlertSeverity;
import com.safehome.model.enums.ZoneType;
import com.safehome.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la sécurité, les alertes et les incidents en temps réel (Écran 2 du cahier des charges).
 * Expose la consultation de l'historique chronologique, le déclenchement du bouton d'urgence
 * et le endpoint de simulation d'incidents (Mode Démo).
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    // Injection du contrat de service AlertService (respect du principe DIP de SOLID)
    private final AlertService alertService;

    /**
     * Récupère l'ensemble des alertes par ordre chronologique inversé.
     *
     * @return Liste d'AlertResponse.
     */
    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        List<AlertResponse> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Récupère uniquement les alertes actives et non résolues.
     *
     * @return Liste d'alertes actives.
     */
    @GetMapping("/active")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        List<AlertResponse> alerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Filtre les alertes par zone géographique.
     *
     * @param zone La zone choisie (ex: CUISINE).
     * @return Alertes de cette zone.
     */
    @GetMapping("/zone/{zone}")
    public ResponseEntity<List<AlertResponse>> getAlertsByZone(@PathVariable ZoneType zone) {
        List<AlertResponse> alerts = alertService.getAlertsByZone(zone);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Filtre les alertes par niveau de criticité.
     *
     * @param severity Le degré de sévérité (INFO, WARNING, CRITICAL).
     * @return Alertes de cette criticité.
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AlertResponse>> getAlertsBySeverity(@PathVariable AlertSeverity severity) {
        List<AlertResponse> alerts = alertService.getAlertsBySeverity(severity);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Simule un incident domestique (Mode Démo : "Déclencher Fuite", "Fumée", etc.).
     *
     * @param request Paramètres de simulation envoyés depuis Flutter.
     * @param authentication Informations d'authentification de l'utilisateur.
     * @return L'alerte générée avec statut HTTP 201 Created.
     */
    @PostMapping("/simulate")
    public ResponseEntity<AlertResponse> simulateIncident(
            @Valid @RequestBody SimulateIncidentRequest request,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        AlertResponse response = alertService.simulateIncident(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Déclenche la procédure de Bouton d'urgence (Écran 2) :
     * Verrouillage général immédiat + déclenchement sirène + alerte critique.
     *
     * @param authentication Informations d'authentification.
     * @return L'alerte d'urgence générée.
     */
    @PostMapping("/emergency")
    public ResponseEntity<AlertResponse> triggerEmergency(Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        AlertResponse response = alertService.triggerEmergency(userEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * Marque une alerte comme résolue / acquittée.
     *
     * @param id L'identifiant de l'alerte à acquitter.
     * @param authentication Informations d'authentification.
     * @return L'alerte mise à jour.
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        AlertResponse response = alertService.resolveAlert(id, userEmail);
        return ResponseEntity.ok(response);
    }
}
