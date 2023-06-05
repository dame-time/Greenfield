package greenfield.model.log;

import greenfield.model.robot.sensors.HealthChecker;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.concurrent.atomic.AtomicInteger;

@Aspect
public class BalancingACKReceiverLogger {
    private final AtomicInteger threadInstanceCounter = new AtomicInteger();

    @Pointcut("execution(* greenfield.model.robot.utils.BalancingACKReceiver.run())")
    public void runMethodPointcut() {}

    @Pointcut("execution(greenfield.model.robot.utils.BalancingACKReceiver.new(..))")
    public void constructorPointcut() {}

    @Before("constructorPointcut()")
    public void beforeConstructorAdvice(JoinPoint joinPoint) {
        if (joinPoint.getTarget() instanceof BalancingACKReceiverLogger) {
            System.out.println("\u001B[34mInstance of BalancingACKReceiver created. Total instances: \u001B[0m" +
                    threadInstanceCounter.incrementAndGet());
        }
    }

    @Before("runMethodPointcut()")
    public void beforeRunMethodAdvice(JoinPoint joinPoint) {
        if (joinPoint.getTarget() instanceof BalancingACKReceiverLogger) {
            System.out.println("\u001B[34mRun method of BalancingACKReceiver is called.\u001B[0m");
        }
    }
}
