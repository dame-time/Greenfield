package greenfield.model.robot.utils;

public class LogicalClock {
    private int clock;

    public LogicalClock() {
        this.clock = 0;
    }

    public int getClock() {
        return clock;
    }

    public void update() {
        ++this.clock;
    }

    public void compareAndUpdate(LogicalClock other) {
        this.clock = Math.max(this.clock, other.clock) + 1;
    }
    public void compareAndUpdate(int other) {
        this.clock = Math.max(this.clock, other) + 1;
    }
}
