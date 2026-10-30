package com.safehome.model.enums;

/**
 * Énumération représentant les scénarios prédéfinis activables en 1 clic (Écran 3).
 */
public enum PresetScenario {
    // Éteint toutes les lumières, ferme le garage, baisse le volet et arme la détection
    DEPART_ABSENCE,

    // Éteint salon/cuisine, verrouille l'entrée et ferme le garage
    NUIT,

    // En cas de fumée : déverrouille l'entrée et ouvre le volet pour faciliter l'évacuation
    SECURITE_INCENDIE
}
