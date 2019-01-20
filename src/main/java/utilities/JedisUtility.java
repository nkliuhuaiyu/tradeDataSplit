package utilities;

import configuration.RedisConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class JedisUtility {

    public static final Logger logger = LogManager.getLogger(JedisUtility.class);
    /**
     * 私有构造器.
     */
    private JedisUtility() {

    }
    private static JedisPool jedisPool;


    /**
     * 获取连接池.
     * @return 连接池实例
     */
    private static JedisPool getPool() {

        if(jedisPool != null){
            return jedisPool;
        }else{
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(RedisConfig.REDIS_POOL_MAXTOTAL);
            config.setMaxIdle(RedisConfig.REDIS_POOL_MAXIDLE);
            config.setMaxWaitMillis(RedisConfig.REDIS_POOL_MAXWAITMILIS);
            config.setTestOnBorrow(RedisConfig.REDIS_POOL_TESTONBORROW);
            config.setTestOnReturn(RedisConfig.REDIS_POOL_TESTONRETURN);
            try {
                /**
                 *如果你遇到 java.net.SocketTimeoutException: Read timed out exception的异常信息
                 *请尝试在构造JedisPool的时候设置自己的超时值. JedisPool默认的超时时间是2秒(单位毫秒)
                 */
                jedisPool = new JedisPool(config, RedisConfig.REDIS_IP, RedisConfig.REDIS_PORT, RedisConfig.REDIS_TIMEOUT);
            } catch (Exception e) {
                logger.warn("JedisPool create throw exception : " , e);
            }
            return jedisPool;
        }
    }

    /**
     *类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例
     *没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。
     */
    private static class RedisUtilHolder{
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static JedisUtility instance = new JedisUtility();
    }

    /**
     *当getInstance方法第一次被调用的时候，它第一次读取
     *RedisUtilHolder.instance，导致RedisUtilHolder类得到初始化；而这个类在装载并被初始化的时候，会初始化它的静
     *态域，从而创建RedisUtil的实例，由于是静态的域，因此只会在虚拟机装载类的时候初始化一次，并由虚拟机来保证它的线程安全性。
     *这个模式的优势在于，getInstance方法并没有被同步，并且只是执行一个域的访问，因此延迟初始化并没有增加任何访问成本。
     */
    public static JedisUtility getInstance() {
        return RedisUtilHolder.instance;
    }

    /**
     * 获取Redis实例.
     * @return Redis工具类实例
     */
    public Jedis getJedis() {
        return getJedis(RedisConfig.REDIS_INDEX_DEFAULT);
    }

    public Jedis getJedis(int index) {
        Jedis jedis  = null;
        int count =0;
        do{
            try{
                jedis = getPool().getResource();
            } catch (Exception e) {
                logger.warn("Get jedis from jedisPool throw exception " , e);
                jedis.close();
            }
            count++;
        }while(jedis==null&&count< RedisConfig.REDIS_POOL_RETRYNUM);
        if( jedis != null) {
            jedis.select(index);
        }else{
            logger.warn("Can not get jedis from jedisPool.");
        }
        return jedis;
    }
}
