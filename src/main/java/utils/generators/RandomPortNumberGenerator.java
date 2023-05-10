package utils.generators;

import java.util.BitSet;

public class RandomPortNumberGenerator {

    private static final int MIN_PORT_NUMBER = 1025;
    private static final int MAX_PORT_NUMBER = 65535;

    // way more efficient than the old method with a dict
    private static final BitSet ports = new BitSet(MAX_PORT_NUMBER - MIN_PORT_NUMBER + 1);

    /**
     * Generates a random available port number.
     *
     * @return an available port number
     * @throws IllegalStateException if no available port is found
     */
    public static int generate() {
        int randomPort;
        synchronized (ports) {
            randomPort = ports.nextClearBit(0) + MIN_PORT_NUMBER;
            if (randomPort > MAX_PORT_NUMBER) {
                throw new IllegalStateException("No available ports found");
            }
            ports.set(randomPort - MIN_PORT_NUMBER);
        }
        return randomPort;
    }

    /**
     * Marks a port as unassigned.
     *
     * @param port the port number to free
     */
    public static void freePort(int port) {
        synchronized (ports) {
            ports.clear(port - MIN_PORT_NUMBER);
        }
    }
}
