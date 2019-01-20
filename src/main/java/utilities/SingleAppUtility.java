package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class SingleAppUtility {
    private static final int FIXED_NET_PROT = 10001;
    private static Logger logger = LogManager.getLogger(SingleAppUtility.class);

    public static boolean isSingleRunning(){
        try {
            final ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress("127.0.0.1" , FIXED_NET_PROT));
            return true;
        } catch (IOException e) {
            logger.error("Server socket initial error." , e);
            return false;
        }
    }
}
