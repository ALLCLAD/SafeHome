package com.safehome.controller;

import com.safehome.dto.device.DeviceCommandRequest;
import com.safehome.dto.device.DeviceResponse;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import com.safehome.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion et le pilotage des équipements de la maison (Écran 1 du cahier des charges).
 * Fournit la liste des appareils par zone ou type, et permet d'exécuter des commandes d'actionneurs.
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    // Injection du contrat de service DeviceService (respect du principe DIP de SOLID)
    private final DeviceService deviceService;

    /**
     * Récupère l'ensemble des équipements de la maison avec leur état actuel.
     *
     * @return Liste complète des appareils au format DTO record.
     */
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        List<DeviceResponse> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    /**
     * Filtre les équipements par zone (SALON, CUISINE, CHAMBRE, GARAGE).
     *
     * @param zone La zone géographique demandée.
     * @return Liste des équipements de cette pièce.
     */
    @GetMapping("/zone/{zone}")
    public ResponseEntity<List<DeviceResponse>> getDevicesByZone(@PathVariable ZoneType zone) {
        List<DeviceResponse> devices = deviceService.getDevicesByZone(zone);
        return ResponseEntity.ok(devices);
    }

    /**
     * Filtre les équipements par type matériel (LIGHT, LOCK, PIR, etc.).
     *
     * @param type Le type d'équipement demandé.
     * @return Liste des appareils du type spécifié.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<DeviceResponse>> getDevicesByType(@PathVariable DeviceType type) {
        List<DeviceResponse> devices = deviceService.getDevicesByType(type);
        return ResponseEntity.ok(devices);
    }

    /**
     * Récupère le détail d'un équipement à partir de son identifiant numérique.
     *
     * @param id L'identifiant de l'appareil.
     * @return Les détails de l'appareil.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        DeviceResponse device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    /**
     * Transmet un ordre de commande à un équipement actionneur (allumer/éteindre, ouvrir/fermer, etc.).
     *
     * @param id L'identifiant de l'équipement commandé.
     * @param request Le payload de la commande encapsulé dans un record.
     * @param authentication Informations sur l'utilisateur connecté émettant la requête.
     * @return Le statut mis à jour de l'équipement.
     */
    @PostMapping("/{id}/command")
    public ResponseEntity<DeviceResponse> sendCommand(
            @PathVariable Long id,
            @Valid @RequestBody DeviceCommandRequest request,
            Authentication authentication) {

        // Extraction de l'email de l'utilisateur authentifié
        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";

        // Exécution de la commande et retour du résultat
        DeviceResponse updatedDevice = deviceService.sendCommand(id, request, userEmail);
        return ResponseEntity.ok(updatedDevice);
    }
}
