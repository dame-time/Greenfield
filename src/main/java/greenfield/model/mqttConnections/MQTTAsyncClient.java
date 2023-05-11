package greenfield.model.mqttConnections;

import org.eclipse.paho.client.mqttv3.*;

public class MQTTAsyncClient {
    private final String clientID;
    private final MqttAsyncClient client;

    public MQTTAsyncClient(String id) throws MqttException {
        this.clientID = id;
        this.client = new MqttAsyncClient(MQTTBroker.URL, this.clientID);
    }

    // The IMqttToken is used to block the code only here, due to the fact that I need to wait the client to connect before sending/receiving data asynchronously
    public void startWithCallback(MqttCallback callback) throws MqttException {
        String lastWillMessage = "Client " + this.clientID + " is dying, cya nerds";

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(this.clientID);
        options.setWill("lastWill/", lastWillMessage.getBytes(), 1, false);
        options.setKeepAliveInterval(60);

        client.setCallback(callback);

        IMqttToken token = client.connect(options);
        try {
            token.waitForCompletion();
        } catch (MqttException e) {
            throw new RuntimeException("Failed to connect to MQTT broker", e);
        }
    }

    public void start() throws MqttException {
        String lastWillMessage = "Client " + this.clientID + " is dying, cya nerds";

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(this.clientID);
        options.setWill("lastWill/", lastWillMessage.getBytes(), 1, false);
        options.setKeepAliveInterval(60);

        IMqttToken token = client.connect(options);
        try {
            token.waitForCompletion();
        } catch (MqttException e) {
            throw new RuntimeException("Failed to connect to MQTT broker", e);
        }
    }


    public void publishMessage(String topicURL, MqttMessage message) throws MqttException {
        client.publish(topicURL, message);
    }

    public void subscribeToTopic(String topicURL) throws MqttException {
        client.subscribe(topicURL, 1);
    }

    public void disconnect() throws MqttException {
        client.disconnect();
    }
}
