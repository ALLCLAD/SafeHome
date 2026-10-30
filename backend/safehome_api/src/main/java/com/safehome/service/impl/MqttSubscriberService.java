package com.safehome.service.impl;

import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import com.safehome.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Service d'écoute des messages MQTT entrants (retours d'état de la simulation ou capteurs réels).
 * Intercepte les messages du canal mqttInputChannel, extrait la zone et le type de l'appareil
 * depuis le topic et délègue la mise à jour à DeviceService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MqttSubscriberService {

    // Service métier gérant les équipements
    private final DeviceService deviceService;

    /**
     * Reçoit chaque message arrivant sur le canal d'écoute MQTT.
     * Topic modèle : safehome_charo_x789/home/{zone}/{device}/status
     *
     * @param message Le message Spring Integration contenant le payload et les en-têtes MQTT.
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleIncomingMqttMessage(Message<?> message) {
        try {
            // Extraction du nom de topic depuis l'en-tête MqttHeaders.RECEIVED_TOPIC
            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
            // Extraction du contenu textuel du message
            String payload = message.getPayload().toString();

            log.info("Message MQTT reçu sur topic '{}' : {}", topic, payload);

            // Si le topic est renseigné, on extrait ses segments
            if (topic != null) {
                // Découpage du topic selon le séparateur '/'
                String[] parts = topic.split("/");

                // Vérifie que le topic possède au moins la structure {prefix}/home/{zone}/{device}/status
                if (parts.length >= 5 && "status".equalsIgnoreCase(parts[parts.length - 1])) {
                    // Segment représentant la zone (ex: salon, cuisine)
                    String zoneStr = parts[parts.length - 3].toUpperCase();
                    // Segment représentant le nom de l'équipement (ex: light, door, smoke)
                    String deviceStr = parts[parts.length - 2].toLowerCase();

                    // Conversion du segment zone vers ZoneType
                    ZoneType zone = ZoneType.valueOf(zoneStr);
                    // Résolution du type d'équipement vers DeviceType
                    DeviceType type = parseDeviceType(deviceStr);

                    // Si le type est reconnu, mise à jour dans la base de données
                    if (type != null) {
                        deviceService.updateDeviceStatus(zone, type, payload);
                    }
                }
            }
        } catch (Exception e) {
            // Capture toute exception pour éviter d'interrompre l'adaptateur de réception MQTT
            log.error("Erreur lors du traitement du message MQTT entrant : {}", e.getMessage());
        }
    }

    /**
     * Associe un libellé textuel de topic à son enum DeviceType correspondant.
     *
     * @param deviceStr Le nom du device dans le topic.
     * @return L'enum DeviceType correspondant ou null si non reconnu.
     */
    private DeviceType parseDeviceType(String deviceStr) {
        return switch (deviceStr) {
            case "light" -> DeviceType.LIGHT;
            case "door", "lock" -> DeviceType.LOCK;
            case "shutter" -> DeviceType.SHUTTER;
            case "thermostat" -> DeviceType.THERMOSTAT;
            case "smoke" -> DeviceType.SMOKE_SENSOR;
            case "water" -> DeviceType.WATER_LEAK;
            case "pir", "motion" -> DeviceType.PIR;
            default -> null;
        };
    }
}
