package greenfield.model.mqttConnections;

import org.eclipse.paho.client.mqttv3.*;

public class MQTTClient {
    private final String clientID;
    private final MqttClient client;

    public MQTTClient(String id) throws MqttException {
        this.clientID = id;
        this.client = new MqttClient(MQTTBroker.URL, this.clientID);
    }

    public void startWithCallback(MqttCallback callback) throws MqttException {
        String lastWillMessage = "Client " + this.clientID + " is dying, cya nerds";

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(this.clientID);
        options.setWill("lastWill/", lastWillMessage.getBytes(), 1, false);
        options.setKeepAliveInterval(60);

        client.setCallback(callback);

        client.connect();
    }

    public void start() throws MqttException {
        String lastWillMessage = "Client " + this.clientID + " is dying, cya nerds";

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(this.clientID);
        options.setWill("lastWill/", lastWillMessage.getBytes(), 1, false);
        options.setKeepAliveInterval(60);

        client.connect();
    }

    public void publishMessage(String topicURL, MqttMessage message) throws MqttException {
        client.publish(topicURL, message);
    }

    public void subscribeToTopic(String topicURL) throws MqttException {
        client.subscribe(topicURL);
    }

    public void disconnect() throws MqttException {
        client.disconnect();
    }
}
