package  configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class RedisConfig {
    private static Logger logger = LogManager.getLogger(RedisConfig.class);

    public static String REDIS_IP;
    public static int REDIS_PORT;
    public static String REDIS_PWD;
    public static int REDIS_TIMEOUT;
    public static int REDIS_POOL_MAXTOTAL;
    public static int REDIS_POOL_MAXIDLE;
    public static int REDIS_POOL_MAXWAITMILIS;
    public static boolean REDIS_POOL_TESTONBORROW;
    public static boolean REDIS_POOL_TESTONRETURN;
    public static int REDIS_POOL_RETRYNUM;
    public static int REDIS_INDEX_TRADESPLITINFO;
    public static int REDIS_INDEX_DEFAULT;

    private RedisConfig() {
    }

    static {
        Properties prop = new Properties();
        try {
            InputStream is = new FileInputStream(new File(System.getProperty("user.dir") + "/redis.properties"));
            if(null == is){
                logger.error("File redis.properties initialize error , return null");
            }
            else{
                prop.load(is);
            }
        } catch (IOException e) {
            logger.error("redis.properties load error!");
            logger.debug("exception stack " , e);
        }

        REDIS_IP = prop.getProperty("redis.ip");
        REDIS_PORT = Integer.valueOf( prop.getProperty("redis.port"));
        REDIS_PWD = prop.getProperty("redis.pwd");
        REDIS_TIMEOUT = Integer.valueOf( prop.getProperty("redis.timeout"));
        REDIS_POOL_MAXTOTAL = Integer.valueOf( prop.getProperty("redis.pool.maxTotal"));
        REDIS_POOL_MAXIDLE = Integer.valueOf( prop.getProperty("redis.pool.maxIdle"));
        REDIS_POOL_MAXWAITMILIS = Integer.valueOf( prop.getProperty("redis.pool.maxWaitMilis"));
        REDIS_POOL_TESTONBORROW = Boolean.valueOf( prop.getProperty("redis.pool.testOnBorrow"));
        REDIS_POOL_TESTONRETURN = Boolean.valueOf( prop.getProperty("redis.pool.testOnReturn"));
        REDIS_POOL_RETRYNUM = Integer.valueOf( prop.getProperty("redis.pool.retryNum"));
        REDIS_INDEX_TRADESPLITINFO = Integer.valueOf( prop.getProperty("redis.index.tradeSplitInfo"));
        REDIS_INDEX_DEFAULT = Integer.valueOf( prop.getProperty("redis.index.default"));
    }
}
