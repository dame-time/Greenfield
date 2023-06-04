package greenfield.model.log;

import greenfield.model.robot.CleaningRobot;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CleaningRobotLogger {

    private static final String COLOR = "\033[0;36m";
    private static final String RESET = "\033[0m";

    @Pointcut("execution(* greenfield.model.robot.CleaningRobot.requestToJoinNetwork(..))")
    public void requestToJoinNetworkPointcut() {}

    @AfterReturning(pointcut = "requestToJoinNetworkPointcut()", returning = "result")
    public void afterRequestToJoinNetwork(JoinPoint joinPoint, boolean result) {
        CleaningRobot robot = (CleaningRobot) joinPoint.getTarget();
        if (result) {
            System.out.println(COLOR + "Successfully added the robot - " + robot.getId() + " - to the network." + RESET);
        } else {
            System.out.println(COLOR + "Failed to add the robot - " + robot.getId() + " - to the network." + RESET);
        }
    }

    @AfterThrowing(pointcut = "requestToJoinNetworkPointcut()", throwing = "error")
    public void afterThrowingRequestToJoinNetwork(JoinPoint joinPoint, Throwable error) {
        CleaningRobot robot = (CleaningRobot) joinPoint.getTarget();
        System.out.println(COLOR + "An error occurred while adding the robot - " + robot.getId() + " - to the network: " + error.getMessage() + RESET);
    }

    @Pointcut("execution(* greenfield.model.robot.CleaningRobot.disconnectFromServer(..))")
    public void disconnectFromServerPointcut() {}

    @AfterReturning(pointcut = "disconnectFromServerPointcut()", returning = "result")
    public void afterDisconnectFromServer(JoinPoint joinPoint, boolean result) {
        CleaningRobot robot = (CleaningRobot) joinPoint.getTarget();
        if (result) {
            System.out.println(COLOR + "Successfully removed the robot - " + robot.getId() + " - from the network." + RESET);
        } else {
            System.out.println(COLOR + "Failed to remove the robot - " + robot.getId() + " - from the network." + RESET);
        }
    }

    @AfterThrowing(pointcut = "disconnectFromServerPointcut()", throwing = "error")
    public void afterThrowingDisconnectFromServer(JoinPoint joinPoint, Throwable error) {
        CleaningRobot robot = (CleaningRobot) joinPoint.getTarget();
        System.out.println(COLOR + "An error occurred while removing the robot - " + robot.getId() + " - from the network: " + error.getMessage() + RESET);
    }
}

