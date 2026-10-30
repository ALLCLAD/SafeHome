package com.safehome.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO représentant la requête de connexion d'un utilisateur à SafeHome.
 * Implémenté sous forme de record Java (immuable, concis et performant).
 *
 * @param email L'adresse email du compte utilisateur (doit être valide et non vide).
 * @param password Le mot de passe associé au compte utilisateur (non vide).
 */
public record LoginRequest(
        // Validation : le champ ne peut pas être null ni composé uniquement d'espaces
        @NotBlank(message = "L'adresse email est requise")
        // Validation : vérifie le respect du format standard email RFC
        @Email(message = "Format d'email invalide")
        String email,

        // Validation : le mot de passe est obligatoire pour authentifier la session
        @NotBlank(message = "Le mot de passe est requis")
        String password
) {
}
