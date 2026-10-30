package com.safehome.service.impl;

import com.safehome.dto.audit.AuditLogResponse;
import com.safehome.model.AuditLog;
import com.safehome.model.User;
import com.safehome.repository.AuditLogRepository;
import com.safehome.repository.UserRepository;
import com.safehome.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation concrète du service de journalisation d'audit (AuditLog).
 * Responsable de la persistance chronologique et de la restitution des actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    // Repository Spring Data pour la table audit_logs
    private final AuditLogRepository auditLogRepository;

    // Repository Spring Data pour la table users
    private final UserRepository userRepository;

    /**
     * Enregistre une trace d'audit pour un utilisateur spécifique.
     *
     * @param user L'utilisateur instigateur de l'action.
     * @param action Description de l'action entreprise.
     * @param targetDevice Équipement cible ou contexte fonctionnel.
     */
    @Override
    @Transactional
    public void log(User user, String action, String targetDevice) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .targetDevice(targetDevice)
                .build();

        auditLogRepository.save(auditLog);

        log.info("Audit enregistré: utilisateur={}, action={}, cible={}",
                user != null ? user.getEmail() : "SYSTEM", action, targetDevice);
    }

    /**
     * Enregistre une trace d'audit à partir d'un email en résolvant l'entité User correspondante.
     *
     * @param email Email de l'utilisateur.
     * @param action Description de l'action.
     * @param targetDevice Équipement ou module concerné.
     */
    @Override
    @Transactional
    public void log(String email, String action, String targetDevice) {
        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        log(user, action, targetDevice);
    }

    /**
     * Retourne tout l'historique d'audit par ordre chronologique inversé.
     *
     * @return Liste ordonnée de AuditLogResponse immuables.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    /**
     * Filtre les entrées d'audit pour un utilisateur donné.
     *
     * @param userId Identifiant de l'utilisateur.
     * @return Liste d'AuditLogResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
