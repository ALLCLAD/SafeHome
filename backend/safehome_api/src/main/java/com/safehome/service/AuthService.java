package com.safehome.service;

import com.safehome.dto.auth.AuthResponse;
import com.safehome.dto.auth.LoginRequest;

/**
 * Contrat de service pour la gestion de l'authentification (Écran 4 du cahier des charges).
 * Respecte le principe d'Inversion des Dépendances (DIP) de SOLID en exposant
 * une abstraction découplée de l'implémentation concrète.
 */
public interface AuthService {

    /**
     * Authentifie un utilisateur à partir de ses identifiants.
     * Vérifie la validité de l'email, la correspondance du mot de passe haché BCrypt,
     * enregistre l'événement dans le journal d'audit et émet un jeton JWT.
     *
     * @param request Contient l'email et le mot de passe en clair.
     * @return Les informations de session et le jeton JWT.
     */
    AuthResponse login(LoginRequest request);
}
