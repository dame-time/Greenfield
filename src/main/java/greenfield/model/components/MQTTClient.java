package greenfield.model.components;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import greenfield.model.Component;

/**
 * A component representing a synchronous MQTT client.
 */
public final class MQTTClient extends Component {

    private IMqttClient mqttClient;
    private final String brokerURL;
    private final String clientID;

    /**
     * Constructs a new MQTTClient with the given broker URL and client ID.
     *
     * @param brokerURL the broker URL
     * @param clientID the client ID
     */
    public MQTTClient(String brokerURL, String clientID) {
        this.brokerURL = brokerURL;
        this.clientID = clientID;
        setName("MQTTClient");
        setID("0");
    }

    /**
     * Connects to the MQTT broker.
     *
     * @throws MqttException if an error occurs while connecting
     */
    public void connect() throws MqttException {
        mqttClient = new MqttClient(brokerURL, clientID);
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(this.clientID);
        connectOptions.setKeepAliveInterval(60);
        mqttClient.connect(connectOptions);
    }

    /**
     * Disconnects from the MQTT broker.
     *
     * @throws MqttException if an error occurs while disconnecting
     */
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
    }

    /**
     * Gets the MQTT client instance.
     *
     * @return the MQTT client instance
     */
    public IMqttClient getMqttClient() {
        return mqttClient;
    }

    /**
     * Sets the name of the MQTTClient component.
     *
     * @param name the name to set
     */
    @Override
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the ID of the MQTTClient component.
     *
     * @param id the ID to set
     */
    @Override
    protected void setID(String id) {
        this.ID = id;
    }
}
