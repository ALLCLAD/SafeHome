package com.safehome.controller;

import com.safehome.dto.auth.AuthResponse;
import com.safehome.dto.auth.LoginRequest;
import com.safehome.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST dédié à l'authentification des utilisateurs (Écran 4 du cahier des charges).
 * Expose uniquement l'endpoint de connexion sécurisée (Login) retournant un token JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Injection par constructeur de l'interface AuthService (respect du principe DIP de SOLID)
    private final AuthService authService;

    /**
     * Endpoint HTTP POST pour authentifier un utilisateur existant.
     * Accessible publiquement sans token préalable.
     *
     * @param request Le DTO record validé contenant email et password.
     * @return Réponse HTTP 200 OK avec le jeton JWT et les données de l'utilisateur.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Exécution de l'authentification et émission du jeton
        AuthResponse response = authService.login(request);
        // Retourne la réponse avec le code HTTP 200 (OK)
        return ResponseEntity.ok(response);
    }
}
