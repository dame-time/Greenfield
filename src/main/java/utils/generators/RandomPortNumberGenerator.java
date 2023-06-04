package utils.generators;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.BitSet;

public class RandomPortNumberGenerator {

    private static final int MIN_PORT_NUMBER = 1025;
    private static final int MAX_PORT_NUMBER = 65535;

    // way more efficient than the old method with a dict
    private static final BitSet ports = new BitSet(MAX_PORT_NUMBER - MIN_PORT_NUMBER + 1);

    public static int generate() {
        int randomPort;
        synchronized (ports) {
            randomPort = ports.nextClearBit(0) + MIN_PORT_NUMBER;
            while (true) {
                if (randomPort > MAX_PORT_NUMBER) {
                    throw new IllegalStateException("No available ports found");
                }
                if (isPortAvailable(randomPort)) {
                    ports.set(randomPort - MIN_PORT_NUMBER);
                    break;
                } else {
                    randomPort = ports.nextClearBit(randomPort - MIN_PORT_NUMBER + 1) + MIN_PORT_NUMBER;
                }
            }
        }
        return randomPort;
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     * @return true if the port is available, else false
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
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
