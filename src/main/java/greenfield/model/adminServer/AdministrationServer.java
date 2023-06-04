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
import utils.data.AirPollutionMeasurement;

import java.io.IOException;
import java.net.URI;
import java.util.*;

// TODO: When a robot leaves or crash delete it from my list
public class AdministrationServer {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    public static final String BASE_URI = "http://" + HOST + ":" + PORT + "/";

    private static HttpServer server;

    private static SingleCrashedRobotHandler singleCrashedRobotHandler;

    public static void startRESTServer() {
        final ResourceConfig rc = new ResourceConfig().packages("greenfield.model.adminServer",
                "greenfield.model.robot");
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        singleCrashedRobotHandler = new SingleCrashedRobotHandler();
        singleCrashedRobotHandler.start();

        System.out.println("Server started on: " + BASE_URI);
    }

    public static void stopRestServerOnKeyPress() throws IOException {
        System.out.println("Hit Enter to stop the server...");
        System.in.read(); // Just blocking the execution
        System.out.println("Stopping server!");
        singleCrashedRobotHandler.shutDownSingleProcessCrashHandler();
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

                List<AirPollutionMeasurement> measurements = new ArrayList<>();
                airPollutionMessage.getMeasurementsList()
                        .forEach(value ->  measurements.add(
                                    new AirPollutionMeasurement(
                                            value.getMeasurement(),
                                            value.getTimestamp(),
                                            value.getSenderID()
                                    )
                                )
                        );

                if (AdministrationServerRegister.getAirPollutionStats().get(topic) == null) {
                    AdministrationServerRegister.putAirPollutionStats(topic, measurements);
                }
                else {
                    try {
                        var ariPollutionCurrentMeasurements = new ArrayList<>(AdministrationServerRegister.getAirPollutionStats().get(topic));
                        ariPollutionCurrentMeasurements.addAll(measurements);
                        AdministrationServerRegister.putAirPollutionStats(topic, ariPollutionCurrentMeasurements);
                    } catch (Exception e) {
                        System.err.println("Exception thrown when receiving a message " +
                                "in the Administration Server :- " + e);
                    }
                }

                System.out.println(AdministrationServerRegister.getAirPollutionStats());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        client.subscribeToTopic("#");
    }

    // TODO: Refactor this into the controller or the view
    public static void main(String[] args) throws IOException, MqttException {

        startRESTServer();

        startMQTTClient();

        stopRestServerOnKeyPress();
    }
}
