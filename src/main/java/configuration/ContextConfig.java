package configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class ContextConfig {
    private static Logger logger = LogManager.getLogger(ContextConfig.class);

    public static String TRADE_DATA_DIR;
    public static String TRADE_DATA_BACKUP_DIR;
    public static String SPLIT_DATA_DIR;
    public static String CONTEXT_FILE_NAME_SUFFIX;
    public static String CONTEXT_TXT_DIR;

    private ContextConfig(){
    }

    static{
        try {
            InputStream inputStream = new FileInputStream(new File(System.getProperty("user.dir") + "/tradeDataSplit.properties"));

            Properties properties = new Properties();
            properties.load(inputStream);

            TRADE_DATA_DIR = properties.getProperty("tradeData.csv.dir");
            TRADE_DATA_BACKUP_DIR = properties.getProperty("tradeData.csv.backup.dir");
            SPLIT_DATA_DIR = properties.getProperty("splitData.dat.dir");
            CONTEXT_FILE_NAME_SUFFIX = properties.getProperty("contextFile.name.suffix");
            CONTEXT_TXT_DIR = properties.getProperty("context.txt.dir");

        } catch (FileNotFoundException e) {
            logger.error("tradeDataSplit.properties file load error." , e);
        } catch (IOException e) {
            logger.error("Property load error." , e);
        }
    }
}
