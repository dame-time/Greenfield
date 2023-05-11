package greenfield.model.robot.sensors;

import greenfield.model.mqttConnections.MQTTAsyncClient;
import greenfield.model.mqttConnections.MQTTClient;
import greenfield.model.mqttConnections.MQTTMessageBuilder;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import proto.AirPollutionMessageOuterClass;
import utils.simulator.Buffer;
import utils.simulator.BufferImpl;
import utils.simulator.Measurement;
import utils.simulator.PM10Simulator;

import java.util.ArrayList;
import java.util.List;

public class AirPollutionSensor extends Thread {
    private final List<Double> avgMeasurements;

    private final PM10Simulator simulator;
    private final Buffer airPollutionBuffer;

    private final String senderID;

    private final MQTTAsyncClient client;
    private final String publishChannel;

    private boolean stopCondition;

    public AirPollutionSensor(String robotID, String publishChannel) throws MqttException {
        this.avgMeasurements = new ArrayList<>();

        this.airPollutionBuffer = new BufferImpl();
        this.simulator = new PM10Simulator(airPollutionBuffer);

        this.senderID = robotID;

        this.client = new MQTTAsyncClient(robotID);
        this.publishChannel = publishChannel;

        this.stopCondition = false;
    }

    private void startSensor() {
        new Thread(simulator).start();
    }

    public void stopSensor() {
        this.stopCondition = true;
    }

    @Override
    public void run() {
        try {
            this.client.startWithCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Data sent!");
                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        startSensor();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (!stopCondition) {

            List<Measurement> measurements = airPollutionBuffer.readAllAndClean();

            processMeasurements(measurements);
            sendMeasurements();
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            this.client.disconnect();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMeasurements(List<Measurement> measurements) {
        int batchSize = 8;
        int iterations = (int) (measurements.size() / 4);
        int remainder = measurements.size() % 4;

        for (int i = 0; i < iterations; i++) {
            final int startElement = i * 4;
            final int endElement = startElement + batchSize;

            double sum = measurements.stream()
                    .filter(e -> measurements.indexOf(e) < endElement + 4 && measurements.indexOf(e) >= startElement)
                    .map(Measurement::getValue).reduce(0.0, Double::sum);
            int dividend = (int) measurements.stream()
                    .filter(e -> measurements.indexOf(e) < endElement + 4 && measurements.indexOf(e) >= startElement)
                    .count();

            double average = sum / dividend;

            avgMeasurements.add(average);
        }

        if (remainder > 0)
            System.out.println("Ignored " + remainder + " measurement, due to buffer length incompatibility.");
    }

    private void sendMeasurements() {
       var airPollutionMessage =    AirPollutionMessageOuterClass.AirPollutionMessage.newBuilder()
                                    .addAllMeasurements(avgMeasurements)
                                    .setTimestamp(System.currentTimeMillis())
                                    .setSenderID(this.senderID)
                                    .build();

       MqttMessage message = new   MQTTMessageBuilder()
                                    .setBytesPayload(airPollutionMessage.toByteArray())
                                    .setQos(0)
                                    .build();

       try {
           this.client.publishMessage(this.publishChannel, message);
       } catch (MqttException e) {
           throw new RuntimeException(e);
       }
    }
}