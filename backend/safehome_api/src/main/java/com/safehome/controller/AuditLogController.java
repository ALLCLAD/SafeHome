package com.safehome.controller;

import com.safehome.dto.audit.AuditLogResponse;
import com.safehome.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST pour la consultation du journal d'audit (Écran 4 du cahier des charges).
 * Permet de visualiser l'historique complet des actions : qui a fait quoi et à quelle heure.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    // Injection de l'interface AuditService (respect du principe DIP)
    private final AuditService auditService;

    /**
     * Récupère l'intégralité du journal d'audit trié par ordre chronologique inversé.
     *
     * @return Liste complète des entrées d'audit.
     */
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        List<AuditLogResponse> logs = auditService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * Filtre les logs d'audit par utilisateur.
     *
     * @param userId Identifiant de l'utilisateur ciblé.
     * @return Liste des logs d'audit de cet utilisateur.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByUser(@PathVariable Long userId) {
        List<AuditLogResponse> logs = auditService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }
}
