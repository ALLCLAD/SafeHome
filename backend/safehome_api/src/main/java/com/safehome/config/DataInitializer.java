package com.safehome.config;

import com.safehome.model.Device;
import com.safehome.model.User;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.Role;
import com.safehome.model.enums.ZoneType;
import com.safehome.repository.DeviceRepository;
import com.safehome.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialiseur de données exécuté au démarrage de l'application SafeHome.
 * En l'absence d'inscription publique, ce composant garantit l'existence
 * d'un compte utilisateur actif par défaut dans la base PostgreSQL ainsi que
 * le parc complet des équipements des 4 zones du cahier des charges.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    // Repository Spring Data pour interagir avec la table users
    private final UserRepository userRepository;

    // Repository Spring Data pour interagir avec la table devices
    private final DeviceRepository deviceRepository;

    // Encodeur BCrypt pour hacher le mot de passe avant enregistrement en base
    private final PasswordEncoder passwordEncoder;

    /**
     * Point d'entrée exécuté automatiquement après le démarrage du contexte Spring Boot.
     *
     * @param args Arguments passés en ligne de commande.
     */
    @Override
    public void run(String... args) {
        // Initialisation du compte utilisateur par défaut avec le rôle unique ROLE_USER
        initUserIfNotExists("user@safehome.com", "user123", "Utilisateur SafeHome", Role.ROLE_USER);

        // Initialisation du parc complet d'équipements selon les 4 zones
        initDefaultDevices();
    }

    /**
     * Vérifie la présence de l'utilisateur par son email, et le crée avec mot de passe haché s'il n'existe pas.
     *
     * @param email Adresse email unique du compte.
     * @param rawPassword Mot de passe en clair à sécuriser via BCrypt.
     * @param fullName Nom et prénom de l'utilisateur.
     * @param role Le rôle unique attribué (ROLE_USER).
     */
    private void initUserIfNotExists(String email, String rawPassword, String fullName, Role role) {
        // Contrôle si un utilisateur avec cette adresse email existe déjà
        if (!userRepository.existsByEmail(email)) {
            // Construction de la nouvelle entité User
            User user = User.builder()
                    // Définition de l'adresse email
                    .email(email)
                    // Hachage cryptographique sécurisé du mot de passe
                    .password(passwordEncoder.encode(rawPassword))
                    // Nom complet
                    .fullName(fullName)
                    // Attribution du rôle unique ROLE_USER
                    .role(role)
                    .build();

            // Persistance de l'utilisateur en base de données
            userRepository.save(user);

            // Log d'information dans la console de démarrage
            log.info("Utilisateur par défaut initialisé : {} ({})", email, role.name());
        }
    }

    /**
     * Initialise l'ensemble des équipements des 4 pièces (Écran 1) s'ils ne sont pas déjà présents.
     */
    private void initDefaultDevices() {
        if (deviceRepository.count() == 0) {
            log.info("Base de données vide : initialisation des équipements domotiques par défaut...");

            // 1. Zone SALON
            createDevice("Lumière Salon", ZoneType.SALON, DeviceType.LIGHT, "{\"state\": \"OFF\", \"brightness\": 80}");
            createDevice("Thermostat Salon", ZoneType.SALON, DeviceType.THERMOSTAT, "{\"target_temp\": 21.0, \"current_temp\": 20.5}");
            createDevice("Détecteur de Présence Salon", ZoneType.SALON, DeviceType.PIR, "{\"motion\": false}");

            // 2. Zone CUISINE
            createDevice("Lumière Cuisine", ZoneType.CUISINE, DeviceType.LIGHT, "{\"state\": \"OFF\", \"brightness\": 100}");
            createDevice("Détecteur de Fumée Cuisine", ZoneType.CUISINE, DeviceType.SMOKE_SENSOR, "{\"alert\": false, \"ppm\": 120}");
            createDevice("Capteur Fuite d'Eau Cuisine", ZoneType.CUISINE, DeviceType.WATER_LEAK, "{\"leak_detected\": false}");

            // 3. Zone CHAMBRE
            createDevice("Lumière Chambre", ZoneType.CHAMBRE, DeviceType.LIGHT, "{\"state\": \"OFF\", \"brightness\": 60}");
            createDevice("Volet Roulant Chambre", ZoneType.CHAMBRE, DeviceType.SHUTTER, "{\"position\": 0}");
            createDevice("Détecteur de Mouvement Chambre", ZoneType.CHAMBRE, DeviceType.PIR, "{\"motion\": false}");

            // 4. Zone GARAGE
            createDevice("Porte de Garage", ZoneType.GARAGE, DeviceType.LOCK, "{\"state\": \"LOCKED\"}");
            createDevice("Lumière Garage", ZoneType.GARAGE, DeviceType.LIGHT, "{\"state\": \"OFF\", \"brightness\": 100}");
            createDevice("Détecteur Mouvement Garage", ZoneType.GARAGE, DeviceType.PIR, "{\"motion\": false}");

            log.info("Parc d'équipements domotiques initialisé avec succès !");
        }
    }

    /**
     * Crée et sauvegarde une entité Device en base de données.
     */
    private void createDevice(String name, ZoneType zone, DeviceType type, String stateValue) {
        Device device = Device.builder()
                .name(name)
                .zone(zone)
                .type(type)
                .stateValue(stateValue)
                .isOnline(true)
                .build();
        deviceRepository.save(device);
    }
}
