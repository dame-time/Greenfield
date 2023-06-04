package greenfield.model.log;

import greenfield.model.mechanic.Mechanic;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MechanicInvariant {
    @Pointcut("execution(* greenfield.model.mechanic.Mechanic.run(..))")
    public void countThreads(){}

    @Before("countThreads()")
    public void beforeThread() {
        int threadCount = countThreadsOfType();
        if(threadCount > 1) {
            throw new RuntimeException("Too many Mechanic threads running!");
        }
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
