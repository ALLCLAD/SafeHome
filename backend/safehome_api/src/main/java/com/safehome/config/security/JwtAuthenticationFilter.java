package com.safehome.config.security;

import com.safehome.model.User;
import com.safehome.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtre de sécurité interceptant chaque requête HTTP entrante.
 * Vérifie la présence du header Authorization "Bearer <token>", valide le JWT,
 * charge l'utilisateur associé et initialise le contexte de sécurité de Spring Security.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Utilitaire JWT pour parser et valider les tokens
    private final JwtUtils jwtUtils;

    // Repository pour charger les détails de l'utilisateur depuis la base de données
    private final UserRepository userRepository;

    /**
     * Méthode principale de filtrage exécutée une seule fois par requête HTTP.
     *
     * @param request La requête HTTP reçue.
     * @param response La réponse HTTP à renvoyer.
     * @param filterChain La chaîne de filtres Spring Security.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Étape 1 : Extraction de la chaîne JWT brute depuis l'en-tête Authorization
            String jwt = parseJwt(request);

            // Étape 2 : Vérification de la présence et de l'intégrité cryptographique du token
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // Étape 3 : Récupération de l'identifiant email présent dans le token
                String email = jwtUtils.getEmailFromToken(jwt);

                // Étape 4 : Recherche de l'utilisateur correspondant dans la base de données PostgreSQL
                User user = userRepository.findByEmail(email).orElse(null);

                // Étape 5 : Si l'utilisateur existe bien, on construit son contexte d'authentification
                if (user != null) {
                    // Création de l'autorité Spring Security basée sur le rôle de l'utilisateur (ROLE_ADMIN ou ROLE_USER)
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

                    // Instanciation du token d'authentification Spring Security avec ses droits
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(authority));

                    // Ajout des détails réseau de la requête (adresse IP, session HTTP)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Injection finale de l'authentification dans le SecurityContext global du thread courant
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Journalisation de toute anomalie rencontrée lors de l'authentification
            logger.error("Impossible de définir l'authentification de l'utilisateur: {}", e);
        }

        // Poursuite normale de l'exécution de la chaîne de filtres pour la requête
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le jeton JWT du header HTTP "Authorization" s'il débute par le préfixe standard "Bearer ".
     *
     * @param request La requête HTTP.
     * @return La chaîne JWT pure ou null si le header est absent ou non conforme.
     */
    private String parseJwt(HttpServletRequest request) {
        // Lecture de la valeur du header Authorization
        String headerAuth = request.getHeader("Authorization");

        // Contrôle de présence et du préfixe Bearer
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Extraction de la sous-chaîne située après "Bearer " (7 caractères)
            return headerAuth.substring(7);
        }

        // Aucun jeton JWT trouvé
        return null;
    }
}
