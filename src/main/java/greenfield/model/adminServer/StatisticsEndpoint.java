package greenfield.model.adminServer;

import utils.data.StatisticsHTTPResponse;
import utils.data.StatisticsResponseStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("robotsInfo")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsEndpoint {
    @Path("/ids")
    @GET
    @Consumes({"application/json", "application/xml"})
    public Response getAllRobots() {
        var allRobots = StatisticsElement.getAllRobotsIDInNetwork();

        if (allRobots.size() < 1)
            return Response.ok("There are currently no robots in the network, try again later!")
                    .status(203).build();

        return Response.ok(allRobots).build();
    }

    @Path("stats/{robotID}/{n}")
    @GET
    @Consumes({"application/json", "application/xml"})
    public Response getRobotStats(@PathParam("robotID") String robotID, @PathParam("n") int n) {
        if (StatisticsElement.robotStatsDoesNotExists(robotID)) {
            StatisticsHTTPResponse response = StatisticsHTTPResponse
                    .newBuilder()
                    .setAverageAirPollution(-2.0)
                    .setResponseStatus(StatisticsResponseStatus.FAILED)
                    .setErrorMessage("There wasn't any robot with such ID: " + robotID)
                    .build();

            return Response.ok(response).status(203).build();
        }

        if (!StatisticsElement.robotStatsNumberIsGreaterThan(robotID, n)) {
            StatisticsHTTPResponse response = StatisticsHTTPResponse
                    .newBuilder()
                    .setAverageAirPollution(StatisticsElement.getRobotNStats(robotID, n))
                    .setResponseStatus(StatisticsResponseStatus.NOT_FULLY_PROCESSED)
                    .setWarningMessage("There wasn't enough data to process, so the" +
                            " average processed is the maximum batch size that I had.")
                    .build();

            return Response.ok(response).build();
        }

        StatisticsHTTPResponse response = StatisticsHTTPResponse
                .newBuilder()
                .setAverageAirPollution(StatisticsElement.getRobotNStats(robotID, n))
                .setResponseStatus(StatisticsResponseStatus.COMPLETED)
                .build();

        return Response.ok(response).build();
    }

    @Path("stats/between/{t1}/{t2}")
    @GET
    @Consumes({"application/json", "application/xml"})
    public Response getRobotsStatsBetweenTimestamps(@PathParam("t1") long t1, @PathParam("t2") long t2) {
        double avg = StatisticsElement.getRobotsStatsBetweenTimestamps(t1, t2);

        if (avg < 0) {
            StatisticsHTTPResponse response = StatisticsHTTPResponse
                    .newBuilder()
                    .setResponseStatus(StatisticsResponseStatus.FAILED)
                    .setErrorMessage("There wasn't any data in the range" +
                            " of timestamps provided [" + t1 + "," + t2 + "]")
                    .build();

            return Response.ok(response).status(203).build();
        }

        StatisticsHTTPResponse response = StatisticsHTTPResponse
                .newBuilder()
                .setAverageAirPollution(avg)
                .setResponseStatus(StatisticsResponseStatus.COMPLETED)
                .build();

        return Response.ok(response).status(200).build();
    }
}
