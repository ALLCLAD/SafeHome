package com.safehome.service.impl;

import com.safehome.config.security.JwtUtils;
import com.safehome.dto.auth.AuthResponse;
import com.safehome.dto.auth.LoginRequest;
import com.safehome.model.User;
import com.safehome.repository.UserRepository;
import com.safehome.service.AuditService;
import com.safehome.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service d'authentification pour SafeHome.
 * Gère uniquement la connexion sécurisée des utilisateurs (aucun endpoint public d'inscription),
 * vérifie l'empreinte BCrypt du mot de passe, consigne la connexion dans l'audit et émet le JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Repository pour l'accès aux utilisateurs enregistrés
    private final UserRepository userRepository;

    // Encodeur de mot de passe BCrypt pour la comparaison sécurisée du hash
    private final PasswordEncoder passwordEncoder;

    // Utilitaire de création et validation des jetons JWT
    private final JwtUtils jwtUtils;

    // Service de traçabilité pour alimenter le journal d'audit
    private final AuditService auditService;

    /**
     * Traite la demande de connexion de l'utilisateur.
     *
     * @param request Le DTO record contenant l'email et le mot de passe.
     * @return La réponse record contenant le token JWT et les informations de profil.
     * @throws BadCredentialsException si l'email ou le mot de passe est erroné.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // Recherche de l'utilisateur par son adresse email dans PostgreSQL
        User user = userRepository.findByEmail(request.email())
                // Si l'utilisateur n'est pas trouvé, rejet immédiat avec exception standard
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        // Comparaison sécurisée entre le mot de passe saisi en clair et le mot de passe haché en BDD
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            // Rejet si le mot de passe ne correspond pas au hash BCrypt
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Journalisation de la connexion réussie dans l'AuditLog
        auditService.log(user, "CONNEXION", "USER_SESSION");

        // Génération du token JWT signé cryptographiquement
        String token = jwtUtils.generateToken(user);

        // Construction et retour de la réponse d'authentification immuable (record)
        return new AuthResponse(
                // Jeton d'accès JWT
                token,
                // Type de jeton standard
                "Bearer",
                // ID de l'utilisateur
                user.getId(),
                // Email vérifié
                user.getEmail(),
                // Nom complet de l'utilisateur
                user.getFullName(),
                // Rôle unique (ROLE_USER)
                user.getRole().name()
        );
    }
}
