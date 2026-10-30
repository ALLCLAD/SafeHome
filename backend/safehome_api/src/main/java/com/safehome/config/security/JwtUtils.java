package com.safehome.config.security;

import com.safehome.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Composant utilitaire responsable des opérations cryptographiques sur les jetons JWT.
 * Gère la signature numérique HMAC-SHA, l'extraction des données (claims)
 * et la vérification de validité temporelle et d'intégrité des tokens.
 */
@Component
public class JwtUtils {

    // Clé secrète de signature injectée depuis application.properties ou variable d'environnement JWT_SECRET
    @Value("${app.jwt.secret:defaultSecretKeyWithAtLeast32BytesForHMACSHA256SafeHome}")
    private String jwtSecret;

    // Durée de validité du jeton en millisecondes (par défaut 24 heures = 86400000 ms)
    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    /**
     * Génère la clé cryptographique HMAC à partir de la chaîne secrète configurée.
     *
     * @return L'instance SecretKey compatible avec la suite algorithmique JJWT.
     */
    private SecretKey getSigningKey() {
        // Conversion de la chaîne de caractères secrète en tableau d'octets UTF-8
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // Création et retour de la clé de chiffrement symétrique HMAC-SHA
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Crée et signe un jeton JWT contenant l'identité et les privilèges de l'utilisateur.
     *
     * @param user L'utilisateur authentifié pour qui générer le jeton.
     * @return Le jeton JWT compacté sous forme de chaîne de caractères sécurisée.
     */
    public String generateToken(User user) {
        // Horodatage actuel marquant le moment d'émission du token
        Date now = new Date();
        // Calcul de la date d'expiration en ajoutant la durée de validité à l'instant présent
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Construction du payload et signature cryptographique du token JWT
        return Jwts.builder()
                // Le sujet principal du token correspond à l'email unique de l'utilisateur
                .subject(user.getEmail())
                // Revendication personnalisée : ID technique de l'utilisateur en BDD
                .claim("id", user.getId())
                // Revendication personnalisée : Rôle attribué (ROLE_ADMIN ou ROLE_USER)
                .claim("role", user.getRole().name())
                // Revendication personnalisée : Nom complet pour affichage direct côté client Flutter
                .claim("fullName", user.getFullName())
                // Date d'émission (Issued At)
                .issuedAt(now)
                // Date d'expiration (Expiration Time)
                .expiration(expiryDate)
                // Signature numérique avec notre clé secrète HMAC-SHA
                .signWith(getSigningKey())
                // Sérialisation finale de la structure compacte JWT
                .compact();
    }

    /**
     * Extrait l'adresse email (sujet) enregistrée dans un jeton JWT.
     *
     * @param token Le jeton JWT signé.
     * @return L'adresse email contenue dans le claim "sub".
     */
    public String getEmailFromToken(String token) {
        // Décodage et vérification de la signature du jeton via le parser JJWT
        Claims claims = Jwts.parser()
                // Définition de la clé de vérification de la signature
                .verifyWith(getSigningKey())
                // Création de l'instance du parser
                .build()
                // Analyse et validation de la signature numérique du token
                .parseSignedClaims(token)
                // Récupération de l'ensemble des données (payload)
                .getPayload();

        // Renvoie le sujet (l'email)
        return claims.getSubject();
    }

    /**
     * Vérifie la validité structurelle, temporelle et cryptographique d'un jeton JWT.
     *
     * @param token Le jeton JWT à valider.
     * @return true si le jeton est authentique et valide, false sinon.
     */
    public boolean validateToken(String token) {
        try {
            // Tente d'analyser le token avec la clé de signature configurée
            Jwts.parser()
                    // Définition de la clé secrète
                    .verifyWith(getSigningKey())
                    // Construction du validateur
                    .build()
                    // Déclenche une exception si le token est falsifié ou expiré
                    .parseSignedClaims(token);
            // Si aucune exception n'est levée, le token est parfaitement valide
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Retourne false en cas de token altéré, expiré, malformé ou vide
            return false;
        }
    }
}
