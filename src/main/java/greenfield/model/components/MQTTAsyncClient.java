package greenfield.model.components;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import greenfield.model.Component;

/**
 * A component representing an asynchronous MQTT client.
 */
public final class MQTTAsyncClient extends Component {

    private IMqttAsyncClient mqttClient;
    private final String brokerURL;
    private final String clientID;

    /**
     * Creates an instance of {@link MQTTAsyncClient} with the given broker URL and client ID.
     *
     * @param brokerURL the URL of the MQTT broker to connect to
     * @param clientID the ID of the MQTT client
     */
    public MQTTAsyncClient(String brokerURL, String clientID) {
        this.brokerURL = brokerURL;
        this.clientID = clientID;
        setName("MQTTAsyncClient");
        setID("1");
    }

    /**
     * Connects the MQTT client to the broker with the given callback.
     *
     * @param callback the {@link MqttCallback} object to use for receiving MQTT messages and connection events
     * @throws MqttException if there was an error connecting to the broker
     */
    public void connect(MqttCallback callback) throws MqttException {
        mqttClient = new MqttAsyncClient(brokerURL, clientID);
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        mqttClient.setCallback(callback);
        mqttClient.connect(connectOptions);
    }

    /**
     * Disconnects the MQTT client from the broker.
     *
     * @throws MqttException if there was an error disconnecting from the broker
     */
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
    }

    /**
     * Returns the MQTT client instance.
     *
     * @return the {@link IMqttAsyncClient} instance
     */
    public IMqttAsyncClient getMqttClient() {
        return mqttClient;
    }

    /**
     * Sets the name of the MQTT asynchronous client component.
     *
     * @param name the name of the component
     */
    @Override
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the ID of the MQTT asynchronous client component.
     *
     * @param id the ID of the component
     */
    @Override
    protected void setID(String id) {
        this.ID = id;
    }
}
