package greenfield.model.log;

import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.grpc.PeerRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;

@Aspect
public class PeerRegistryLogger {

    private static final String PURPLE = "\033[0;35m";
    private static final String RESET = "\033[0m";

    @Pointcut("execution(* greenfield.model.robot.grpc.PeerRegistry.addPeer(..))")
    public void addPeerPointcut() {}

    @AfterReturning("addPeerPointcut()")
    public void afterAddPeer(JoinPoint joinPoint) {
        PeerRegistry registry = (PeerRegistry) joinPoint.getTarget();
        String id = (String) joinPoint.getArgs()[0];
        int port = (int) joinPoint.getArgs()[2];
        CleaningRobot referenceRobot = getReferenceRobot(registry);
        System.out.println(PURPLE + "The robot - " + referenceRobot.getId() + " - now knows how to " +
                "communicate with robot ~ " + id + " ~ at port + " + port + " +" + RESET);
    }

    @Pointcut("execution(* greenfield.model.robot.grpc.PeerRegistry.removePeer(..))")
    public void removePeerPointcut() {}

    @AfterReturning("removePeerPointcut()")
    public void afterRemovePeer(JoinPoint joinPoint) {
        PeerRegistry registry = (PeerRegistry) joinPoint.getTarget();
        String id = (String) joinPoint.getArgs()[0];
        CleaningRobot referenceRobot = getReferenceRobot(registry);
        System.out.println(PURPLE + "The robot - " + id + " - now is now removed " +
                "from peer + " + referenceRobot.getId() + " + list: " + registry.getConnectedPeers() + RESET);
    }

    private CleaningRobot getReferenceRobot(PeerRegistry registry) {
        try {
            Field field = registry.getClass().getDeclaredField("referenceRobot");
            field.setAccessible(true);
            return (CleaningRobot) field.get(registry);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to access referenceRobot", e);
        }
    }
}