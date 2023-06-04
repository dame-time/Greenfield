package greenfield.model.log;

import greenfield.model.robot.sensors.HealthChecker;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class HealthCheckerLogger {
    @Pointcut("execution(* greenfield.model.robot.sensors.HealthChecker.askForMechanic(..))")
    public void loggingPointcut() {}

    @Before("loggingPointcut()")
    public void beforeRunning(JoinPoint joinPoint) {
        HealthChecker checker = (HealthChecker) joinPoint.getTarget();
        String robotId = checker.getReferenceRobot().getId();
        System.out.println("\u001B[34mRobot -" + robotId + " - needs the mechanic\u001B[0m");
    }
}
