package com.safehome.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration centrale de Spring Security pour l'API SafeHome.
 * Met en place une architecture stateless par JWT, configure le filtre d'authentification,
 * désactive CSRF (inutile avec JWT stateless) et active les règles CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Filtre personnalisé vérifiant et validant le token JWT à chaque requête HTTP
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Origines CORS autorisées injectées depuis application.properties
    @Value("${cors.allowed-origins:*}")
    private String corsAllowedOrigins;

    /**
     * Définit l'algorithme de hachage de mot de passe (BCrypt).
     *
     * @return L'instance BCryptPasswordEncoder pour le chiffrement et la vérification des mots de passe.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Renvoie l'encodeur standard sécurisé BCrypt
        return new BCryptPasswordEncoder();
    }

    /**
     * Fournit l'AuthenticationManager de Spring Security.
     *
     * @param config Configuration d'authentification globale.
     * @return L'AuthenticationManager configuré.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Récupération de l'instance standard d'AuthenticationManager
        return config.getAuthenticationManager();
    }

    /**
     * Définit la chaîne de filtres de sécurité HTTP (SecurityFilterChain).
     *
     * @param http L'objet HttpSecurity à configurer.
     * @return La chaîne SecurityFilterChain assemblée.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuration des permissions cross-origin (CORS)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Désactivation de la protection CSRF devenue superflue en architecture stateless REST avec JWT
                .csrf(AbstractHttpConfigurer::disable)
                // Gestion de session sans état (STATELESS) : aucune session HTTP ne sera créée côté serveur
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Définition fine des autorisations d'accès aux différentes routes de l'API
                .authorizeHttpRequests(auth -> auth
                        // Autorise l'accès public au endpoint d'authentification (/api/auth/login)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Autorise l'accès public à la documentation interactive Swagger UI & OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Autorise les requêtes de pré-vol HTTP OPTIONS pour le protocole CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Toutes les autres routes requièrent obligatoirement un token JWT valide
                        .anyRequest().authenticated()
                )
                // Positionnement de notre filtre JWT avant le filtre standard d'authentification par formulaire
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Construction et retour de la chaîne de sécurité
        return http.build();
    }

    /**
     * Configure les stratégies CORS pour autoriser les clients frontend (Flutter mobile, web, etc.).
     *
     * @return La source de configuration CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Création de l'objet de configuration CORS
        CorsConfiguration configuration = new CorsConfiguration();

        // Si l'origine configurée est le joker "*", on utilise les patterns pour éviter les conflits d'authentification
        if ("*".equals(corsAllowedOrigins.trim())) {
            // Autorisation de tous les domaines clients
            configuration.addAllowedOriginPattern("*");
        } else {
            // Découpage et application de la liste explicite des origines autorisées
            configuration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
        }

        // Autorisation de l'ensemble des verbes HTTP nécessaires pour l'API REST domotique
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Autorisation de tous les en-têtes HTTP (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));
        // Autorisation des identifiants (credentials)
        configuration.setAllowCredentials(true);

        // Association de cette configuration à tous les chemins d'URL de l'API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Application de la configuration à l'ensemble du périmètre "/**"
        source.registerCorsConfiguration("/**", configuration);
        // Retour de la source
        return source;
    }
}
