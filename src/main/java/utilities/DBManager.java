package utilities;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;

public class DBManager {
    private static final String MYBATIS_CONFIG_FILE="mybatis-config.xml";

    private static final Logger logger = LogManager.getLogger(DBManager.class);
    public static SqlSessionFactory sqlSessionFactory;

    static {
        try{
             Reader reader = Resources.getResourceAsReader(MYBATIS_CONFIG_FILE);
             sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }catch (Exception e){
            logger.error("Mybatis config file load exception : " + e.getMessage() , e);
        }
    }
}
