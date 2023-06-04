package greenfield.model.robot;

import greenfield.model.adminServer.AdministrationServer;

import com.google.gson.Gson;

import greenfield.model.robot.sensors.AirPollutionSensor;
import greenfield.model.robot.sensors.HealthChecker;
import utils.data.CleaningRobotHTTPSetup;
import utils.data.CleaningRobotStatus;
import utils.data.Position;
import utils.generators.RandomPortNumberGenerator;
import utils.generators.RandomStringGenerator;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

// TODO: Create a way to assign a unique ID
@XmlRootElement(name = "robot")
public class CleaningRobot {
    private String id;
    private String mqttListenerChannel;
    private int gRPCListenerChannel;
    private Position<Integer, Integer> position;

    private CleaningRobotStatus cleaningRobotStatus;

    private transient AirPollutionSensor sensor;
    private transient HealthChecker healthChecker;

    public CleaningRobot() {
        this.id = RandomStringGenerator.generate();

        this.mqttListenerChannel = "greenfield/pollution/district";

        this.position = new Position<>(-1, -1);

        this.cleaningRobotStatus = CleaningRobotStatus.JOINING;

        this.gRPCListenerChannel = RandomPortNumberGenerator.generate();
    }

    public boolean requestToJoinNetwork() {
        try {
            String serverURL = AdministrationServer.BASE_URI;
            String addRequestURL = "robots/add";

            URL url = new URL(serverURL + addRequestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true); // so the connection will register the output;

            String requestBody = String.format("{\"id\": \"%s\"," +
                    " \"mqttListenerChannel\": \"%s\"," +
                    " \"gRPCListenerChannel\": %d}", this.id, this.mqttListenerChannel, this.gRPCListenerChannel);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            boolean result = false;
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }

                Gson gson = new Gson();
                CleaningRobotHTTPSetup response = gson.fromJson(sb.toString(), CleaningRobotHTTPSetup.class);

                this.position = response.getDistrictCell().position;

                String publishingDistrictNumber = String.valueOf(response.getDistrictCell().districtNumber);
                List<CleaningRobot> robots = response.getCurrentCleaningRobots();

                var otherRobots = robots.stream().filter(e -> !Objects.equals(e.getId(), this.id)).toList();

                this.healthChecker = new HealthChecker(this, otherRobots);

                this.cleaningRobotStatus = CleaningRobotStatus.ACTIVE;

                this.mqttListenerChannel += publishingDistrictNumber;

                this.sensor = new AirPollutionSensor(this.id, this.mqttListenerChannel);

                this.sensor.start();
                this.healthChecker.start();

                result = true;
            } else {
                this.cleaningRobotStatus = CleaningRobotStatus.DEAD;
            }

            conn.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.cleaningRobotStatus = CleaningRobotStatus.DEAD;

        return false;
    }

    public boolean disconnectFromServer() {
        try {
            String serverURL = AdministrationServer.BASE_URI;
            String deleteRequestURL = "robots/delete/" + this.getId();

            URL url = new URL(serverURL + deleteRequestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            boolean result = false;
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                this.sensor.stopSensor();
                this.healthChecker.shutDownHealthChecker();

                freeRobotGRPCListenerPort();

                this.cleaningRobotStatus = CleaningRobotStatus.DEAD;

                result = true;
            }

            conn.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void crash() {
        this.sensor.stopSensor();
        this.healthChecker.crashHealthChecker();
        this.healthChecker.shutDownHealthChecker();
    }

    public void fix() {
        this.healthChecker.askForMechanic();
    }

    public String getId() {
        return id;
    }

    public String getMqttListenerChannel() {
        return mqttListenerChannel;
    }

    public Position<Integer, Integer> getPosition() {
        return position;
    }

    public CleaningRobotStatus getCleaningRobotStatus() {
        return cleaningRobotStatus;
    }

    public int getgRPCListenerChannel() {
        return gRPCListenerChannel;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMqttListenerChannel(String mqttListenerChannel) {
        this.mqttListenerChannel = mqttListenerChannel;
    }

    public void setPosition(Position<Integer, Integer> position) {
        this.position = position;
    }

    public void setCleaningRobotStatus(CleaningRobotStatus cleaningRobotStatus) {
        this.cleaningRobotStatus = cleaningRobotStatus;
    }

    public void setgRPCListenerChannel(int gRPCListenerChannel) {
        this.gRPCListenerChannel = gRPCListenerChannel;
    }

    private void freeRobotGRPCListenerPort() {
        RandomPortNumberGenerator.freePort(this.gRPCListenerChannel);
    }
}
