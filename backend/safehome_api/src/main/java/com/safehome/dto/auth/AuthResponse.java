package com.safehome.dto.auth;

/**
 * DTO représentant la réponse renvoyée suite à une authentification réussie.
 * Fournit le jeton JWT d'accès ainsi que le profil résumé de l'utilisateur connecté.
 * Implémenté sous forme de record Java pour garantir l'immutabilité des données de session.
 *
 * @param token Le jeton JWT signé généré par le serveur.
 * @param type Le type de jeton (par défaut "Bearer").
 * @param id L'identifiant unique de l'utilisateur en base de données.
 * @param email L'adresse email de l'utilisateur.
 * @param fullName Le nom complet de l'utilisateur.
 * @param role Le rôle unique assigné (ROLE_USER).
 */
public record AuthResponse(
        // Le jeton JWT encodé
        String token,
        // Le préfixe standard du header Authorization HTTP (Bearer)
        String type,
        // L'identifiant unique en base de données
        Long id,
        // L'adresse email du compte connecté
        String email,
        // Le nom et prénom complet de l'utilisateur
        String fullName,
        // Le rôle (ROLE_USER)
        String role
) {
    /**
     * Constructeur compact avec valeur par défaut pour le type de token ("Bearer").
     */
    public AuthResponse(String token, Long id, String email, String fullName, String role) {
        // Appelle le constructeur canonique avec "Bearer" par défaut
        this(token, "Bearer", id, email, fullName, role);
    }
}
