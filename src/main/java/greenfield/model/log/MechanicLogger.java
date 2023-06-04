package greenfield.model.log;

import greenfield.model.mechanic.Mechanic;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * The purpose of this class is just for testing locally, not for usage in a distributed system.
 */
@Aspect
public class MechanicLogger {
    @Pointcut("execution(* greenfield.model.mechanic.Mechanic.run(..))")
    public void countThreads(){}

    @Before("countThreads()")
    public void beforeThread() {
        int threadCount = countThreadsOfType();
        System.out.println("\033[0;37m" + "Current mechanic thread count: " + threadCount + "\033[0m");
    }

    @After("countThreads()")
    public void afterThread() {
        int threadCount = countThreadsOfType() - 1;
        System.out.println("\033[0;37m" + "Current mechanic thread count: " + threadCount + "\033[0m");
    }

    private int countThreadsOfType() {
        int count = 0;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getClass().equals(Mechanic.class)) {
                count++;
            }
        }
        return count;
    }
}
