package com.safehome.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

/**
 * Configuration de l'intégration MQTT avec HiveMQ / Broker MQTT (Paho Client).
 * Définit les canaux entrants/sortants et les connecteurs Spring Integration.
 */
@Configuration
@Slf4j
public class MqttConfig {

    // URL du broker MQTT injectée depuis application.properties (${MQTT_BROKER_URL:tcp://broker.hivemq.com:1883})
    @Value("${mqtt.broker.url:tcp://broker.hivemq.com:1883}")
    private String brokerUrl;

    // Identifiant de ce client backend pour le broker
    @Value("${mqtt.client.id:safehome_backend}")
    private String clientId;

    // Préfixe personnalisé des topics SafeHome
    @Value("${mqtt.topic.prefix:safehome_charo_x789}")
    private String topicPrefix;

    /**
     * Fabrique de client Paho configurant les options de connexion (KeepAlive, CleanSession, etc.).
     *
     * @return L'instance configurée de MqttPahoClientFactory.
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        // Création de la fabrique Paho par défaut
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        // Création de l'objet d'options de connexion MQTT
        MqttConnectOptions options = new MqttConnectOptions();

        // Si l'URL contient plusieurs adresses séparées par virgule ou une seule
        options.setServerURIs(new String[]{brokerUrl});
        // Active le nettoyage de session à la reconnexion
        options.setCleanSession(true);
        // Intervalle de maintien de connexion (keep-alive) en secondes
        options.setKeepAliveInterval(30);
        // Reconnexion automatique en cas de coupure réseau
        options.setAutomaticReconnect(true);
        // Délai maximal de connexion en secondes
        options.setConnectionTimeout(10);

        // Association des options à la factory
        factory.setConnectionOptions(options);
        // Retourne la factory prête
        return factory;
    }

    /**
     * Canal Spring Integration dédié à l'envoi de messages vers le broker.
     *
     * @return Le MessageChannel sortant.
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        // Canal direct synchrone en mémoire
        return new DirectChannel();
    }

    /**
     * Gestionnaire sortant publiant les messages sur le broker MQTT.
     *
     * @return Le MessageHandler configuré.
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        // Identifiant unique pour le producteur afin d'éviter les collisions
        String outboundClientId = clientId + "_out_" + System.currentTimeMillis();
        // Création du handler Paho sortant
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(outboundClientId, mqttClientFactory());
        // Envoi asynchrone pour ne pas bloquer les requêtes HTTP de l'API
        messageHandler.setAsync(true);
        // Niveau de qualité de service (QoS 1 : délivrance garantie au moins une fois)
        messageHandler.setDefaultQos(1);
        // Topic par défaut
        messageHandler.setDefaultTopic(topicPrefix + "/home/default");
        // Retour du handler
        return messageHandler;
    }

    /**
     * Canal Spring Integration dédié à la réception des retours d'états et capteurs depuis le broker.
     *
     * @return Le MessageChannel entrant.
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        // Canal direct synchrone pour les messages entrants
        return new DirectChannel();
    }

    /**
     * Adaptateur d'écoute (subscriber) s'abonnant aux topics d'états : {prefix}/home/+/+/status
     *
     * @return Le MessageProducer d'écoute des messages MQTT.
     */
    @Bean
    public MessageProducer inbound() {
        // Identifiant unique pour le consommateur
        String inboundClientId = clientId + "_in_" + System.currentTimeMillis();
        // Définition du pattern d'écoute (wildcard "+" pour intercepter toutes les zones et tous les devices)
        String inboundTopic = topicPrefix + "/home/+/+/status";

        // Instanciation de l'adaptateur de messages Paho
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(inboundClientId, mqttClientFactory(), inboundTopic);

        // Délai de complétion en millisecondes
        adapter.setCompletionTimeout(5000);
        // Convertisseur de message par défaut
        adapter.setConverter(new DefaultPahoMessageConverter());
        // QoS 1 pour la réception
        adapter.setQos(1);
        // Redirection des messages reçus vers le canal entrant mqttInputChannel
        adapter.setOutputChannel(mqttInputChannel());

        // Retour de l'adaptateur
        return adapter;
    }
}
