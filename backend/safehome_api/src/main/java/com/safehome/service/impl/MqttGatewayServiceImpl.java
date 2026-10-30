package com.safehome.service.impl;

import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import com.safehome.service.MqttGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service MqttGatewayService.
 * Construit dynamiquement les topics MQTT selon la convention du cahier des charges :
 * safehome_charo_x789/home/{zone}/{device}/set
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MqttGatewayServiceImpl implements MqttGatewayService {

    // Canal Spring Integration configuré pour acheminer les messages sortants vers le broker
    private final MessageChannel mqttOutboundChannel;

    // Préfixe personnalisé (ex: safehome_charo_x789)
    @Value("${mqtt.topic.prefix:safehome_charo_x789}")
    private String topicPrefix;

    /**
     * Publie une commande de pilotage vers un équipement spécifique.
     *
     * @param zone La zone ciblée (ex: SALON).
     * @param type Le type d'appareil (ex: LIGHT).
     * @param payload Le contenu JSON ou texte de la commande.
     */
    @Override
    public void publishCommand(ZoneType zone, DeviceType type, String payload) {
        // Construction du nom d'équipement en minuscules (ex: LIGHT -> light, WATER_LEAK -> water)
        String deviceName = resolveDeviceTopicName(type);
        // Construction de la zone en minuscules (ex: SALON -> salon)
        String zoneName = zone.name().toLowerCase();
        // Construction du topic complet respectant la nomenclature officielle
        String topic = String.format("%s/home/%s/%s/set", topicPrefix, zoneName, deviceName);

        // Appel de la méthode générique d'envoi
        publish(topic, payload);
    }

    /**
     * Publie une chaîne de caractères sur un topic MQTT quelconque.
     *
     * @param topic Le chemin du topic.
     * @param payload Le payload à émettre.
     */
    @Override
    public void publish(String topic, String payload) {
        try {
            // Création du message Spring Integration avec le topic en en-tête MQTT
            Message<String> message = MessageBuilder.withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .build();

            // Expédition du message sur le canal sortant
            mqttOutboundChannel.send(message);

            // Log d'information
            log.info("Message MQTT publié avec succès sur '{}' : {}", topic, payload);
        } catch (Exception e) {
            // Log d'erreur si le broker est injoignable ou en cas de problème de transmission
            log.error("Échec de la publication MQTT sur le topic '{}' : {}", topic, e.getMessage());
        }
    }

    /**
     * Convertit l'enum DeviceType vers le libellé de topic MQTT spécifié dans le document d'architecture.
     *
     * @param type L'enum du device.
     * @return Le segment de chaîne pour le topic.
     */
    private String resolveDeviceTopicName(DeviceType type) {
        // Correspondances spécifiques selon le document d'architecture
        return switch (type) {
            case LIGHT -> "light";
            case LOCK -> "door";
            case SHUTTER -> "shutter";
            case THERMOSTAT -> "thermostat";
            case SMOKE_SENSOR -> "smoke";
            case WATER_LEAK -> "water";
            case PIR -> "pir";
        };
    }
}
