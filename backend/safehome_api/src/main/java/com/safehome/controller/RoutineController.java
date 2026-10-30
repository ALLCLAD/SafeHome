package com.safehome.controller;

import com.safehome.dto.routine.RoutineRequest;
import com.safehome.dto.routine.RoutineResponse;
import com.safehome.dto.routine.ScenarioExecutionResult;
import com.safehome.dto.routine.TriggerScenarioRequest;
import com.safehome.model.enums.PresetScenario;
import com.safehome.service.RoutineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les scénarios automatiques et les routines (Écran 3 du cahier des charges).
 * Permet l'activation en 1 clic des modes (Départ, Nuit, Sécurité Incendie)
 * et le CRUD complet des routines programmables.
 */
@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {

    // Injection de l'interface RoutineService (DIP de SOLID)
    private final RoutineService routineService;

    /**
     * Déclenche un scénario prédéfini en 1 clic.
     * Modes supportés : DEPART_ABSENCE, NUIT, SECURITE_INCENDIE.
     *
     * @param request Contient le mode de scénario à exécuter.
     * @param authentication Données de l'utilisateur connecté.
     * @return Résultat d'exécution avec la liste des actions appliquées.
     */
    @PostMapping("/scenarios/trigger")
    public ResponseEntity<ScenarioExecutionResult> triggerScenario(
            @Valid @RequestBody TriggerScenarioRequest request,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        ScenarioExecutionResult result = routineService.triggerPresetScenario(request.scenario(), userEmail);
        return ResponseEntity.ok(result);
    }

    /**
     * Raccourci d'activation directe d'un scénario par URL.
     * Ex: POST /api/routines/scenarios/NUIT/trigger
     */
    @PostMapping("/scenarios/{scenario}/trigger")
    public ResponseEntity<ScenarioExecutionResult> triggerScenarioByName(
            @PathVariable PresetScenario scenario,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        ScenarioExecutionResult result = routineService.triggerPresetScenario(scenario, userEmail);
        return ResponseEntity.ok(result);
    }

    /**
     * Liste toutes les routines configurées.
     */
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> getAllRoutines() {
        List<RoutineResponse> routines = routineService.getAllRoutines();
        return ResponseEntity.ok(routines);
    }

    /**
     * Récupère une routine par son identifiant.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> getRoutineById(@PathVariable Long id) {
        RoutineResponse routine = routineService.getRoutineById(id);
        return ResponseEntity.ok(routine);
    }

    /**
     * Crée une nouvelle routine programmable.
     */
    @PostMapping
    public ResponseEntity<RoutineResponse> createRoutine(
            @Valid @RequestBody RoutineRequest request,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        RoutineResponse created = routineService.createRoutine(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Met à jour une routine existante.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> updateRoutine(
            @PathVariable Long id,
            @Valid @RequestBody RoutineRequest request,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        RoutineResponse updated = routineService.updateRoutine(id, request, userEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * Active ou désactive rapidement une routine en 1 clic.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RoutineResponse> toggleRoutine(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        RoutineResponse toggled = routineService.toggleRoutine(id, userEmail);
        return ResponseEntity.ok(toggled);
    }

    /**
     * Supprime une routine.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutine(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        routineService.deleteRoutine(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exécute manuellement une routine.
     */
    @PostMapping("/{id}/run")
    public ResponseEntity<Void> runRoutine(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication != null ? authentication.getName() : "ANONYMOUS";
        routineService.executeRoutine(id, userEmail);
        return ResponseEntity.ok().build();
    }
}
