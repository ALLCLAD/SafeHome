package com.safehome.service;

import com.safehome.dto.routine.RoutineRequest;
import com.safehome.dto.routine.RoutineResponse;
import com.safehome.dto.routine.ScenarioExecutionResult;
import com.safehome.model.enums.PresetScenario;

import java.util.List;

/**
 * Contrat de service pour la gestion des scénarios automatiques et routines (Écran 3).
 * Respecte le principe d'Inversion des Dépendances (DIP) de SOLID.
 */
public interface RoutineService {

    /**
     * Exécute un scénario prédéfini en 1 clic (DEPART_ABSENCE, NUIT, SECURITE_INCENDIE).
     *
     * @param scenario Le mode sélectionné.
     * @param userEmail Email de l'utilisateur ayant activé le scénario.
     * @return Résultat et actions appliquées sur les équipements.
     */
    ScenarioExecutionResult triggerPresetScenario(PresetScenario scenario, String userEmail);

    /**
     * Récupère la liste de toutes les routines programmées.
     *
     * @return Liste de RoutineResponse.
     */
    List<RoutineResponse> getAllRoutines();

    /**
     * Récupère une routine par son identifiant.
     *
     * @param id Identifiant de la routine.
     * @return Le DTO record de la routine.
     */
    RoutineResponse getRoutineById(Long id);

    /**
     * Crée une nouvelle routine.
     *
     * @param request Données de la routine.
     * @param userEmail Email de l'utilisateur créateur.
     * @return La routine créée.
     */
    RoutineResponse createRoutine(RoutineRequest request, String userEmail);

    /**
     * Met à jour une routine existante.
     *
     * @param id Identifiant de la routine à modifier.
     * @param request Nouvelles valeurs.
     * @param userEmail Email de l'utilisateur.
     * @return La routine mise à jour.
     */
    RoutineResponse updateRoutine(Long id, RoutineRequest request, String userEmail);

    /**
     * Active ou désactive rapidement une routine (toggle en 1 clic).
     *
     * @param id Identifiant de la routine.
     * @param userEmail Email de l'utilisateur.
     * @return La routine avec son nouvel état.
     */
    RoutineResponse toggleRoutine(Long id, String userEmail);

    /**
     * Supprime une routine.
     *
     * @param id Identifiant de la routine à supprimer.
     * @param userEmail Email de l'utilisateur.
     */
    void deleteRoutine(Long id, String userEmail);

    /**
     * Exécute manuellement une routine par son identifiant.
     *
     * @param id Identifiant de la routine.
     * @param userEmail Email de l'utilisateur.
     */
    void executeRoutine(Long id, String userEmail);
}
