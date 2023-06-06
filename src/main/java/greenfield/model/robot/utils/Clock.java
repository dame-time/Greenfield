package greenfield.model.robot.utils;

import java.util.Random;

public class Clock {
    private final long offset;

    public Clock() {
        this.offset = 0;
    }

    public Clock(boolean hasOffset, int offsetRange) {
        if (hasOffset)
            this.offset = Math.abs(new Random().nextLong()) % offsetRange;
        else
            this.offset = 0;
    }

    public long getTime() {
        return offset + System.currentTimeMillis();
    }
}
