package greenfield.model.adminServer;

import greenfield.model.mqttConnections.MQTTAsyncClient;
import greenfield.model.robot.CleaningRobot;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import proto.AirPollutionMessageOuterClass;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class AdministrationServer {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    public static final String BASE_URI = "http://" + HOST + ":" + PORT + "/";

    public static final Map<String, List<Double>> airPollutionStats = new HashMap<>();

    private static HttpServer server;

    public static void startRESTServer() {
        final ResourceConfig rc = new ResourceConfig().packages("greenfield.model.adminServer", "greenfield.model");
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        System.out.println("Server started on: " + BASE_URI);
    }

    public static void stopRestServerOnKeyPress() throws IOException {
        System.out.println("Hit any key to stop the server...");
        System.in.read();
        System.out.println("Stopping server!");
        server.shutdownNow();
        System.out.println("Server stopped!");
    }

    public static void startMQTTClient() throws MqttException {
        MQTTAsyncClient client = new MQTTAsyncClient("AdministrationServer");

        client.startWithCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("Connection to MQTT broker lost into the administration server!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Message delivered to topic :- " + topic);

                var airPollutionMessage = AirPollutionMessageOuterClass.AirPollutionMessage.parseFrom(message.getPayload());

                if (CleaningRobotsElement.getRobots()
                        .stream()
                        .noneMatch(e -> Objects.equals(e.getId(), airPollutionMessage.getSenderID()))
                ) {
                    System.err.println("The Cleaning Robot with ID - " + airPollutionMessage.getSenderID() +
                            " - is not authorized to send messages!");

                    return;
                }

                synchronized (airPollutionStats) {
                    if (airPollutionStats.get(topic) == null) {
                        airPollutionStats.put(topic, airPollutionMessage.getMeasurementsList());
                    }
                    else {
                        try {
                            var ariPollutionCurrentMeasurements = new ArrayList<>(airPollutionStats.get(topic));
                            ariPollutionCurrentMeasurements.addAll(airPollutionMessage.getMeasurementsList());
                            airPollutionStats.put(topic, ariPollutionCurrentMeasurements);
                        } catch (Exception e) {
                            System.err.println("Exception thrown when receiving a message " +
                                    "in the Administration Server :- " + e);
                        }
                    }

                    System.out.println(airPollutionStats);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        client.subscribeToTopic("#");
    }

    public static void main(String[] args) throws IOException, MqttException {

        startRESTServer();

        startMQTTClient();

        CleaningRobot r0 = new CleaningRobot();
        CleaningRobot r1 = new CleaningRobot();
        CleaningRobot r2 = new CleaningRobot();

        System.out.println("R0: ");
        r0.requestToJoinNetwork();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("R1: ");
        r1.requestToJoinNetwork();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("R2: ");
        r2.requestToJoinNetwork();

//        r1.disconnectFromServer();
//        r2.disconnectFromServer();

        stopRestServerOnKeyPress();

    }
}
