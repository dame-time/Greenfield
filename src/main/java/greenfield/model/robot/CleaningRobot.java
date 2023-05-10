package greenfield.model.robot;

import greenfield.model.adminServer.AdministrationServer;

import com.google.gson.Gson;

import utils.data.CleaningRobotHTTPSetup;
import utils.data.Position;
import utils.generators.RandomStringGenerator;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@XmlRootElement(name = "robot")
public class CleaningRobot {
    private String id;
    private String mqttListenerChannel;
    private Position<Integer, Integer> position;
    private AirPollutionSensor sensor;

    public CleaningRobot() {
        this.id = RandomStringGenerator.generate();
        this.mqttListenerChannel = "greenfield/pollution/district";
        this.position = new Position<>(-1, -1);
        this.sensor = new AirPollutionSensor();
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

            String requestBody = String.format("{\"id\": \"%s\", \"mqttListenerChannel\": \"%s\"}", this.id, this.mqttListenerChannel);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            boolean result = false;
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Successfully added the robot to the network.");

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()))); // printing the JSON response
                StringBuilder sb = new StringBuilder();
                String output;
                System.out.println("Output from Server .... \n");
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                    sb.append(output);
                }

                Gson gson = new Gson();
                CleaningRobotHTTPSetup response = gson.fromJson(sb.toString(), CleaningRobotHTTPSetup.class);

                Position<Integer, Integer> newPosition = response.getPosition();
                List<CleaningRobot> robots = response.getCurrentCleaningRobots(); // TODO: use this to connect with others through MQTT

                this.position = newPosition;

                result = true;
            } else {
                System.out.println("Failed to add the robot to the network. Response code: " + responseCode);
            }

            conn.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                System.out.println("Successfully removed the robot to the network.");

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()))); // printing the JSON response
                String output;
                System.out.println("Output from Server .... \n");
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                }

                result = true;
            } else {
                System.out.println("Failed to remove the robot from the network. Response code: " + responseCode);
            }

            conn.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setMqttListenerChannel(String mqttListenerChannel) {
        this.mqttListenerChannel = mqttListenerChannel;
    }

    public void setPosition(Position<Integer, Integer> position) {
        this.position = position;
    }
}
