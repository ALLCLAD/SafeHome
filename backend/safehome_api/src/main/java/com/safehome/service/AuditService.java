package com.safehome.service;

import com.safehome.dto.audit.AuditLogResponse;
import com.safehome.model.User;

import java.util.List;

/**
 * Contrat de service pour la gestion et consultation du journal d'audit (Écran 4).
 * Permet de respecter l'Inversion des Dépendances (DIP) de SOLID.
 */
public interface AuditService {

    /**
     * Enregistre une entrée d'audit à partir de l'entité User.
     */
    void log(User user, String action, String targetDevice);

    /**
     * Enregistre une entrée d'audit à partir de l'adresse email.
     */
    void log(String email, String action, String targetDevice);

    /**
     * Récupère l'intégralité du journal d'audit trié par ordre chronologique inversé.
     *
     * @return Liste d'AuditLogResponse.
     */
    List<AuditLogResponse> getAllAuditLogs();

    /**
     * Récupère les logs d'audit associés à un utilisateur spécifique.
     *
     * @param userId Identifiant de l'utilisateur.
     * @return Liste ordonnée d'AuditLogResponse.
     */
    List<AuditLogResponse> getAuditLogsByUser(Long userId);
}
