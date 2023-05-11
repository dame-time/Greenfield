package greenfield.model.mqttConnections;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTMessageBuilder {
    private String payload;
    private int qos = 0; // default qos

    private byte[] bytesPayload;

    public MQTTMessageBuilder setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public MQTTMessageBuilder setBytesPayload(byte[] payload) {
        this.bytesPayload = payload;
        return this;
    }

    public MQTTMessageBuilder setQos(int qos) {
        this.qos = qos;
        return this;
    }

    public MqttMessage build() {
        if (payload == null && bytesPayload == null) {
            throw new IllegalStateException("Payload must not be null");
        }

        MqttMessage message;

        if (payload != null)
            message = new MqttMessage(payload.getBytes());
        else
            message = new MqttMessage(bytesPayload);

        message.setQos(qos);
        return message;
    }
}
