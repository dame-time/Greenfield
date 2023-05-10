package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.CleaningRobotHTTPResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("robots")
@Produces(MediaType.APPLICATION_JSON)
public class CleaningRobotsEndpoint {
    @GET
    @Consumes({"application/json", "application/xml"})
    public Response getCurrentRobots() {
        return Response.ok(CleaningRobotsElement.getRobots()).build();
    }

    @Path("/add")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addRobot(CleaningRobot robot) {
        if (CleaningRobotsElement.canInsertRobot(robot))
        {
            try {
                var districtCell = CleaningRobotsElement.insertRobot(robot);
                CleaningRobotHTTPResponse response = new CleaningRobotHTTPResponse(districtCell.position, CleaningRobotsElement.getRobots());
                return Response.ok(response).build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return Response.status(502, "The Cleaning Robot with ID: " + robot.getId() + " already" +
                " exists in the city").build();
    }

    @Path("/mutate/{oldRobotID}")
    @PUT
    @Consumes({"application/json", "application/xml"})
    public Response mutateRobot(CleaningRobot newRobot, @PathParam("oldRobotID") String oldRobotID) { // TODO: Fix this from int to String
        if (CleaningRobotsElement.mutateRobot(oldRobotID, newRobot))
            return Response.ok(CleaningRobotsElement.getRobots()).build();

        return Response.status(502, "The Cleaning Robot with ID: " + oldRobotID + " does not" +
                " exists in the city").build();
    }

    @Path("/delete/{robotID}")
    @DELETE
    @Consumes({"application/json", "application/xml"})
    public Response deleteRobot(@PathParam("robotID") String robotID) {
        if (CleaningRobotsElement.removeRobot(robotID))
            return Response.ok(CleaningRobotsElement.getRobots()).build();

        return Response.status(502, "The Cleaning Robot with ID: " + robotID + " does not" +
                " exists in the city").build();
    }
}
