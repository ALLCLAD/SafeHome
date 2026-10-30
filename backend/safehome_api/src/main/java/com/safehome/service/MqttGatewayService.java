package com.safehome.service;

import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;

/**
 * Service spécialisé dans les échanges réseau avec le broker MQTT (HiveMQ).
 * Respecte le principe de Responsabilité Unique (SRP) de SOLID en isolant
 * la sérialisation, la construction des topics et l'envoi de messages IoT.
 */
public interface MqttGatewayService {

    /**
     * Publie une commande de pilotage vers un équipement spécifique.
     * Topic standard : {prefix}/home/{zone}/{device}/set
     *
     * @param zone La zone de l'équipement (ex: SALON).
     * @param type Le type de l'équipement (ex: LIGHT).
     * @param payload Le contenu de la commande en JSON.
     */
    void publishCommand(ZoneType zone, DeviceType type, String payload);

    /**
     * Publie une commande sur un topic explicite personnalisé.
     *
     * @param topic Le chemin de topic MQTT complet.
     * @param payload Le contenu textuel ou JSON à transmettre.
     */
    void publish(String topic, String payload);
}
