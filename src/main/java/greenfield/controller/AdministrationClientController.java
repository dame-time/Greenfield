package greenfield.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import utils.data.StatisticsHTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdministrationClientController {
    private static final String serverURL = "http://localhost:8080/";

    public List<String> getAllRobotIDs() throws IOException {
        String endpointURL = serverURL + "robotsInfo/ids";

        String responseString = getResponseFrom(endpointURL).toString();

        if (responseString.equals("There are currently no robots in the network, try again later!"))
            return new ArrayList<>();

        return new Gson().fromJson(responseString, new TypeToken<List<String>>(){}.getType());
    }

    public StatisticsHTTPResponse getRobotStats(String robotID, int n) throws IOException {
        String endpointURL = serverURL + "robotsInfo/stats/" + robotID + "/" + n;
        return new Gson().fromJson(getResponseFrom(endpointURL).toString(), StatisticsHTTPResponse.class);
    }

    public StatisticsHTTPResponse getRobotsStatsBetweenTimestamps(long t1, long t2) throws IOException {
        String endpointURL = serverURL + "robotsInfo/stats/between/" + t1 + "/" + t2;
        return new Gson().fromJson(getResponseFrom(endpointURL).toString(), StatisticsHTTPResponse.class);
    }

    private StringBuilder getResponseFrom(String endpointURL) throws IOException {
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