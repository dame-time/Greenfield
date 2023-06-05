package greenfield.model.robot.sensors;

import greenfield.model.mqttConnections.MQTTAsyncClient;
import greenfield.model.mqttConnections.MQTTMessageBuilder;
import greenfield.model.robot.CleaningRobot;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import proto.AirPollutionMessageOuterClass;
import utils.data.AirPollutionMeasurement;
import utils.simulator.Buffer;
import utils.simulator.BufferImpl;
import utils.simulator.Measurement;
import utils.simulator.PM10Simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AirPollutionSensor extends Thread {
    private final List<AirPollutionMeasurement> avgMeasurements;

    private final PM10Simulator simulator;
    private final Buffer airPollutionBuffer;

    private final CleaningRobot referenceRobot;

    private final MQTTAsyncClient client;

    private boolean stopCondition;

    public AirPollutionSensor(CleaningRobot referenceRobot) throws MqttException {
        this.avgMeasurements = new ArrayList<>();

        this.airPollutionBuffer = new BufferImpl();
        this.simulator = new PM10Simulator(airPollutionBuffer);

        this.referenceRobot = referenceRobot;

        this.client = new MQTTAsyncClient(referenceRobot.getId());

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

            List<Measurement> batch = measurements.stream()
                    .filter(e -> measurements.indexOf(e) < endElement + 4 && measurements.indexOf(e) >= startElement)
                    .toList();

            double sum = batch.stream().map(Measurement::getValue).reduce(0.0, Double::sum);
            int dividend = batch.size();

            double average = sum / dividend;

            long maxTimestampInBatch = batch.stream()
                    .max(Comparator.comparing(Measurement::getTimestamp))
                    .map(Measurement::getTimestamp)
                    .orElse(System.currentTimeMillis());

            avgMeasurements.add(new AirPollutionMeasurement(average, maxTimestampInBatch, this.referenceRobot.getId()));
        }

        if (remainder > 0)
            System.out.println("\033[0;33m" + "Ignored " + remainder + " measurement, due to buffer length incompatibility." + "\033[0m");
    }

    private void sendMeasurements() {
        List<AirPollutionMessageOuterClass.AirPollutionMeasurement> airPollutionMeasurements = new ArrayList<>();

        for (var avgMeasurement : avgMeasurements)
            airPollutionMeasurements.add(AirPollutionMessageOuterClass
                                        .AirPollutionMeasurement
                                        .newBuilder()
                                        .setMeasurement(avgMeasurement.getMeasurement())
                                        .setSenderID(avgMeasurement.getRobotID())
                                        .setTimestamp(avgMeasurement.getTimestamp())
                                        .build()
            );

        var airPollutionMessage =    AirPollutionMessageOuterClass.AirPollutionMessage.newBuilder()
                                    .addAllMeasurements(airPollutionMeasurements)
                                    .setTimestamp(System.currentTimeMillis())
                                    .setSenderID(this.referenceRobot.getId())
                                    .build();

        MqttMessage message = new    MQTTMessageBuilder()
                                    .setBytesPayload(airPollutionMessage.toByteArray())
                                    .setQos(0)
                                    .build();

        try {
            this.client.publishMessage(this.referenceRobot.getMqttListenerChannel(), message);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
