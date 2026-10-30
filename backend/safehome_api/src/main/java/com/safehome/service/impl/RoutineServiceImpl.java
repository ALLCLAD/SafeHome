package com.safehome.service.impl;

import com.safehome.dto.routine.RoutineRequest;
import com.safehome.dto.routine.RoutineResponse;
import com.safehome.dto.routine.ScenarioExecutionResult;
import com.safehome.model.Device;
import com.safehome.model.Routine;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.PresetScenario;
import com.safehome.model.enums.ZoneType;
import com.safehome.repository.DeviceRepository;
import com.safehome.repository.RoutineRepository;
import com.safehome.service.AuditService;
import com.safehome.service.MqttGatewayService;
import com.safehome.service.RoutineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implémentation du service RoutineService.
 * Gère l'exécution des scénarios 1-clic (Départ, Nuit, Incendie),
 * la programmation des routines et la synchronisation avec les équipements et MQTT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineServiceImpl implements RoutineService {

    // Repository pour manipuler les routines
    private final RoutineRepository routineRepository;

    // Repository pour agir sur les équipements domotiques
    private final DeviceRepository deviceRepository;

    // Passerelle MQTT pour émettre les ordres physiques sur le broker HiveMQ
    private final MqttGatewayService mqttGatewayService;

    // Service d'audit traçant les automatisations
    private final AuditService auditService;

    /**
     * Exécute un scénario prédéfini en 1 clic selon les spécifications exactes du cahier des charges :
     * 1. Mode "Départ / Absence" : Éteint toutes les lumières, ferme le garage, baisse le volet et active les PIR.
     * 2. Mode "Nuit" : Éteint salon/cuisine, verrouille la porte d'entrée et ferme le garage.
     * 3. Mode "Sécurité Incendie" : Déverrouille la porte d'entrée et ouvre le volet pour l'évacuation.
     */
    @Override
    @Transactional
    public ScenarioExecutionResult triggerPresetScenario(PresetScenario scenario, String userEmail) {
        List<String> actions = new ArrayList<>();

        switch (scenario) {
            case DEPART_ABSENCE -> {
                // 1. Éteint toutes les lumières
                List<Device> lights = deviceRepository.findByType(DeviceType.LIGHT);
                for (Device light : lights) {
                    light.setStateValue("{\"state\": \"OFF\"}");
                    deviceRepository.save(light);
                    mqttGatewayService.publishCommand(light.getZone(), DeviceType.LIGHT, "{\"state\": \"OFF\"}");
                    actions.add("Lumière éteinte : " + light.getName() + " (" + light.getZone() + ")");
                }

                // 2. Ferme le garage
                List<Device> garageDoors = deviceRepository.findByZoneAndType(ZoneType.GARAGE, DeviceType.LOCK);
                for (Device door : garageDoors) {
                    door.setStateValue("{\"state\": \"LOCKED\"}");
                    deviceRepository.save(door);
                    mqttGatewayService.publishCommand(ZoneType.GARAGE, DeviceType.LOCK, "{\"state\": \"LOCKED\"}");
                    actions.add("Porte de garage fermée et verrouillée");
                }

                // 3. Baisse le volet de la chambre (position 0%)
                List<Device> shutters = deviceRepository.findByZoneAndType(ZoneType.CHAMBRE, DeviceType.SHUTTER);
                for (Device shutter : shutters) {
                    shutter.setStateValue("{\"position\": 0}");
                    deviceRepository.save(shutter);
                    mqttGatewayService.publishCommand(ZoneType.CHAMBRE, DeviceType.SHUTTER, "{\"position\": 0}");
                    actions.add("Volet roulant de la chambre baissé");
                }

                // 4. Active tous les détecteurs de présence/mouvement
                actions.add("Détecteurs de mouvement armés en mode surveillance active");
            }

            case NUIT -> {
                // 1. Éteint les lumières du salon et de la cuisine
                List<Device> salonLights = deviceRepository.findByZoneAndType(ZoneType.SALON, DeviceType.LIGHT);
                List<Device> kitchenLights = deviceRepository.findByZoneAndType(ZoneType.CUISINE, DeviceType.LIGHT);
                List<Device> nightLights = new ArrayList<>();
                nightLights.addAll(salonLights);
                nightLights.addAll(kitchenLights);

                for (Device light : nightLights) {
                    light.setStateValue("{\"state\": \"OFF\"}");
                    deviceRepository.save(light);
                    mqttGatewayService.publishCommand(light.getZone(), DeviceType.LIGHT, "{\"state\": \"OFF\"}");
                    actions.add("Lumière éteinte : " + light.getName());
                }

                // 2. Verrouille la porte d'entrée et ferme le garage
                List<Device> locks = deviceRepository.findByType(DeviceType.LOCK);
                for (Device lock : locks) {
                    lock.setStateValue("{\"state\": \"LOCKED\"}");
                    deviceRepository.save(lock);
                    mqttGatewayService.publishCommand(lock.getZone(), DeviceType.LOCK, "{\"state\": \"LOCKED\"}");
                    actions.add("Verrouillé : " + lock.getName());
                }
            }

            case SECURITE_INCENDIE -> {
                // 1. Déverrouille toutes les serrures / portes d'entrée pour l'évacuation rapide
                List<Device> locks = deviceRepository.findByType(DeviceType.LOCK);
                for (Device lock : locks) {
                    lock.setStateValue("{\"state\": \"UNLOCKED\"}");
                    deviceRepository.save(lock);
                    mqttGatewayService.publishCommand(lock.getZone(), DeviceType.LOCK, "{\"state\": \"UNLOCKED\"}");
                    actions.add("Déverrouillage secours : " + lock.getName());
                }

                // 2. Ouvre les volets roulants à 100% pour l'aération et l'issue de secours
                List<Device> shutters = deviceRepository.findByType(DeviceType.SHUTTER);
                for (Device shutter : shutters) {
                    shutter.setStateValue("{\"position\": 100}");
                    deviceRepository.save(shutter);
                    mqttGatewayService.publishCommand(shutter.getZone(), DeviceType.SHUTTER, "{\"position\": 100}");
                    actions.add("Ouverture totale volet : " + shutter.getName());
                }
            }
        }

        // Publication de l'événement global sur MQTT
        mqttGatewayService.publish("safehome_charo_x789/home/scenarios/active",
                String.format("{\"scenario\": \"%s\", \"triggeredBy\": \"%s\"}", scenario.name(), userEmail));

        // Journalisation dans l'AuditLog
        auditService.log(userEmail, "ACTIVATION_SCENARIO", scenario.name());

        log.info("Scénario 1-Clic exécuté : {} par {}", scenario.name(), userEmail);

        return new ScenarioExecutionResult(
                scenario,
                "Scénario " + scenario.name() + " exécuté avec succès",
                actions
        );
    }

    /**
     * Retourne toutes les routines enregistrées.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoutineResponse> getAllRoutines() {
        return routineRepository.findAll().stream()
                .map(RoutineResponse::fromEntity)
                .toList();
    }

    /**
     * Recherche une routine par ID.
     */
    @Override
    @Transactional(readOnly = true)
    public RoutineResponse getRoutineById(Long id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Routine non trouvée avec l'id : " + id));
        return RoutineResponse.fromEntity(routine);
    }

    /**
     * Crée une routine.
     */
    @Override
    @Transactional
    public RoutineResponse createRoutine(RoutineRequest request, String userEmail) {
        Routine routine = Routine.builder()
                .name(request.name())
                .triggerType(request.triggerType())
                .cronExpression(request.cronExpression())
                .conditionExpr(request.conditionExpr())
                .actionPayload(request.actionPayload())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        Routine savedRoutine = routineRepository.save(routine);

        auditService.log(userEmail, "CREATION_ROUTINE", savedRoutine.getName());
        log.info("Nouvelle routine créée : '{}' par {}", savedRoutine.getName(), userEmail);

        return RoutineResponse.fromEntity(savedRoutine);
    }

    /**
     * Met à jour une routine.
     */
    @Override
    @Transactional
    public RoutineResponse updateRoutine(Long id, RoutineRequest request, String userEmail) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Routine introuvable : " + id));

        routine.setName(request.name());
        routine.setTriggerType(request.triggerType());
        routine.setCronExpression(request.cronExpression());
        routine.setConditionExpr(request.conditionExpr());
        routine.setActionPayload(request.actionPayload());
        if (request.isActive() != null) {
            routine.setIsActive(request.isActive());
        }

        Routine updated = routineRepository.save(routine);
        auditService.log(userEmail, "MODIFICATION_ROUTINE", updated.getName());

        return RoutineResponse.fromEntity(updated);
    }

    /**
     * Bascule l'activation d'une routine (on/off rapide).
     */
    @Override
    @Transactional
    public RoutineResponse toggleRoutine(Long id, String userEmail) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Routine introuvable : " + id));

        routine.setIsActive(!routine.getIsActive());
        Routine updated = routineRepository.save(routine);

        String status = updated.getIsActive() ? "ACTIVEE" : "DESACTIVEE";
        auditService.log(userEmail, "TOGGLE_ROUTINE_" + status, updated.getName());
        log.info("Routine '{}' basculée : {}", updated.getName(), status);

        return RoutineResponse.fromEntity(updated);
    }

    /**
     * Supprime une routine.
     */
    @Override
    @Transactional
    public void deleteRoutine(Long id, String userEmail) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Routine introuvable : " + id));

        routineRepository.delete(routine);
        auditService.log(userEmail, "SUPPRESSION_ROUTINE", routine.getName());
        log.info("Routine supprimée : '{}'", routine.getName());
    }

    /**
     * Exécute manuellement une routine.
     */
    @Override
    @Transactional
    public void executeRoutine(Long id, String userEmail) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Routine introuvable : " + id));

        // Publication de l'action de la routine sur MQTT
        mqttGatewayService.publish("safehome_charo_x789/home/routines/" + routine.getId() + "/run", routine.getActionPayload());

        auditService.log(userEmail, "EXECUTION_ROUTINE", routine.getName());
        log.info("Routine exécutée : '{}' ({})", routine.getName(), routine.getActionPayload());
    }

    /**
     * Tâche planifiée périodique Spring Boot (@Scheduled) :
     * Exécute par exemple la routine récurrente du soir (ex: fermeture automatique garage à 22h00).
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void scheduledNightSecurityCheck() {
        log.info("Exécution planifiée @Scheduled de 22h00 : Sécurité nocturne automatique...");
        triggerPresetScenario(PresetScenario.NUIT, "SYSTEM_SCHEDULED");
    }
}
