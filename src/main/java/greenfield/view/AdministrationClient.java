package greenfield.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import greenfield.model.adminServer.AdministrationServer;
import utils.data.StatisticsHTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

// TODO: Create all this function into a class in the controller
public class AdministrationClient {
    private static final String serverURL = AdministrationServer.BASE_URI;

    public static void main(String[] args) throws IOException {
        System.out.println("\033[1;33m" + "Welcome to the Administration Client! Please use the following commands:" + "\033[0m");
        System.out.println("\033[1;32m" + "robots - To get all robot IDs" + "\033[0m");
        System.out.println("\033[1;32m" + "<robotID> <n> - To get stats for robot with ID <robotID> for last <n> measurements" + "\033[0m");
        System.out.println("\033[1;32m" + "<t1> <t2> - To get stats between timestamps <t1> and <t2>" + "\033[0m");
        System.out.println("\033[1;32m" + "q - To terminate the client" + "\033[0m");

        Scanner scanner = new Scanner(System.in);

        boolean hasUserPressedEsc = false;

        while (!hasUserPressedEsc) {
            System.out.print("\033[1;36m" + "Enter command: " + "\033[0m");
            String command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (command.equalsIgnoreCase("q")) {
                System.out.println("\033[0;34m" + "Terminating client. Goodbye!" + "\033[0m");
                hasUserPressedEsc = true;
                continue;
            }

            if (parts.length == 1 && parts[0].equalsIgnoreCase("robots")) {
                List<String> robotIDs = getAllRobotIDs();
                System.out.println("\033[0;34m" + "Robot IDs: " + robotIDs + "\033[0m");
            } else if (parts.length == 2 && isInteger(parts[1])) {
                var stats = getRobotStats(parts[0], Integer.parseInt(parts[1])).getAverageAirPollution();
                System.out.println("\033[0;35m" + "Stats for robot " + parts[0] + ": " + stats + "\033[0m");
            } else if (parts.length == 2 && isLong(parts[0]) && isLong(parts[1])) {
                var stats = getRobotsStatsBetweenTimestamps(Long.parseLong(parts[0]), Long.parseLong(parts[1])).getAverageAirPollution();
                System.out.println("\033[0;35m" + "Stats between timestamps " + parts[0] + " and " + parts[1] + ": " + stats + "\033[0m");
            } else {
                System.out.println("\033[0;31m" + "Invalid command, please try again." + "\033[0m");
            }
        }
    }

    private static boolean isInteger(String strNum) {
        if (strNum == null)
            return false;

        try {
            int value = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    private static boolean isLong(String strNum) {
        if (strNum == null)
            return false;

        try {
            long value = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    private static List<String> getAllRobotIDs() throws IOException {
        String endpointURL = serverURL + "robotsInfo/ids";

        // This should be the way to correctly deserialize a List of strings
        return new Gson().fromJson(getResponseFrom(endpointURL).toString(), new TypeToken<List<String>>(){}.getType());
    }

    private static StatisticsHTTPResponse getRobotStats(String robotID, int n) throws IOException {
        String endpointURL = serverURL + "robotsInfo/stats/" + robotID + "/" + n;

        return new Gson().fromJson(getResponseFrom(endpointURL).toString(), StatisticsHTTPResponse.class);
    }

    public static StatisticsHTTPResponse getRobotsStatsBetweenTimestamps(long t1, long t2) throws IOException {
        String endpointURL = serverURL + "robotsInfo/stats/between/" + t1 + "/" + t2;

        return new Gson().fromJson(getResponseFrom(endpointURL).toString(), StatisticsHTTPResponse.class);
    }

    private static StringBuilder getResponseFrom(String endpointURL) throws IOException {
        URL url = new URL(endpointURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        StringBuilder sb = new StringBuilder();
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        conn.disconnect();
        return sb;
    }
}
