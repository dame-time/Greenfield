package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class AdministrationServer {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    public static final String BASE_URI = "http://" + HOST + ":" + PORT + "/";

    public static void main(String[] args) throws IOException {
        final ResourceConfig rc = new ResourceConfig().packages("greenfield.model.adminServer", "greenfield.model");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        System.out.println("Server started on: " + BASE_URI);

//        CleaningRobot r0 = new CleaningRobot();
//        CleaningRobot r1 = new CleaningRobot();
//        CleaningRobot r2 = new CleaningRobot();
//
//        System.out.println("R0: ");
//        r0.requestToJoinNetwork();
//        System.out.println("R1: ");
//        r1.requestToJoinNetwork();
//        System.out.println("R2: ");
//        r2.requestToJoinNetwork();
//
//        r1.disconnectFromServer();
//        r2.disconnectFromServer();

        System.out.println("Hit any key to stop the server...");
        System.in.read();
        System.out.println("Stopping server!");
        server.shutdownNow();
        System.out.println("Server stopped!");
    }
}
