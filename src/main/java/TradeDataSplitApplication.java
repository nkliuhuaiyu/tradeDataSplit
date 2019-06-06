import business.Constant;
import business.DataConverter;
import business.FileContext;
import configuration.ContextConfig;
import configuration.RedisConfig;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import service.QRTradeDataService;
import utilities.FileUtility;
import utilities.JedisUtility;
import utilities.SingleAppUtility;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

public class TradeDataSplitApplication {

    private static Logger logger = LogManager.getLogger(TradeDataSplitApplication.class);

    public static void main(String[] args) throws Exception {
        try {
            if(!SingleAppUtility.isSingleRunning()){
                logger.error(new StringBuffer().append(TradeDataSplitApplication.class).append("TradeDataSplitApplication has already running.").toString());
                return;
            }
            List<File> tradeDataFiles = FileUtility.getSortedFileList(ContextConfig.TRADE_DATA_DIR);
            for(File tradeDataFile : tradeDataFiles){
                String filePathName = tradeDataFile.getAbsolutePath();
                logger.debug("+++filename:{}" , filePathName);

                RandomAccessFile targetFile = new RandomAccessFile(filePathName , "r");
                MappedByteBuffer mappedBuffer = targetFile.getChannel().map(FileChannel.MapMode.READ_ONLY , 0 , targetFile.length());
                long fileLength = targetFile.length();
                FileContext context = FileUtility.generateFileContext(filePathName , fileLength , mappedBuffer);
                targetFile.getChannel().close();
                targetFile.close();

                Jedis jedis = JedisUtility.getInstance().getJedis(RedisConfig.REDIS_INDEX_TRADESPLITINFO);
                String fileDateInfo = filePathName.substring(filePathName.lastIndexOf(Constant.FILE_NAME_PREFIX) + Constant.FILE_NAME_PREFIX.length()).replace(".csv" , "");
                final String contextRedisKey = Constant.CONTEXT_REDIS_KEY_PREFIX + fileDateInfo;
                Map<String , String> contextMap =  jedis.hgetAll(contextRedisKey);
                if(contextMap == null || contextMap.size() == 0){
                    //initialize the context to redis
                    jedis.hset(contextRedisKey , Constant.CONTEXT_FILENAME , context.getFileName());
                    jedis.hset(contextRedisKey , Constant.CONTEXT_FILELSIZE , String.valueOf(context.getFileSize()));
                    jedis.hset(contextRedisKey , Constant.CONTEXT_FILEMD5 , context.getFileMD5());
                    jedis.hset(contextRedisKey , Constant.CONTEXT_LINE_COUNT , String.valueOf(context.getFileTotalLines() - 1));//-1 means exclude the header
                    jedis.hset(contextRedisKey , Constant.CONTEXT_PROGRESS_LINE_COUNT , String.valueOf(context.getConvertedLines()));
                    jedis.hset(contextRedisKey , Constant.CONTEXT_ERROR_LINE_COUNT , String.valueOf(context.getErrorLines()));
                    jedis.hset(contextRedisKey , Constant.CONTEXT_ERROR_INFO , String.valueOf(context.getErrorInfo()));
                }

                int processedLine = Integer.valueOf( jedis.hget(contextRedisKey  , Constant.CONTEXT_PROGRESS_LINE_COUNT) );
                logger.info("---------------Start from " + String.valueOf(processedLine));
                Reader in = new FileReader(filePathName);
                Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);
                int counter = 0;
                int progressFull = context.getFileTotalLines();
                final String dataOutlineRedisKey = Constant.DATEFILE_OUTLINE_REDIS_KEY_PREFIX + fileDateInfo;

                for(CSVRecord record : records){
                    if(counter < processedLine){
                        counter ++;
                        continue;
                    }

                    //update record to the database，card_no/bus_id/trade_date唯一标识一次交易
                    String cardNo = null;
                    String busId = null;
                    String tradeDate = null;
                    try {
                        //线路编号
                        cardNo = record.get("card_no");
                        //车辆编号
                        busId = record.get("bus_id");
                        //POS机编号
                        tradeDate = record.get("trade_date");
                        //csv中只要有记录就将交易的状态更新为1，代表已回传，0 代表未回传，数据库中该字段的默认值应为0
                        int affectedRows = QRTradeDataService.updateTradeData(cardNo , busId ,tradeDate , filePathName , (short)1);

                        if(affectedRows != 1){
                            logger.error("Trade data updated affectedRows=" + affectedRows + " ,cardNo=" + cardNo + ",busId=" + busId + ",tradeDate=" + tradeDate);
                        }
                    } catch (Exception e) {
                        logger.error("Update trade data fail." , e);
                    }

                    //将csv Record 中的tradeData插入到数据库的fz_csv表中
                    int affectedRows = QRTradeDataService.insertTradeDate(record);
                    if(affectedRows != 1){
                        logger.error("+++Trade data updated affectedRows=" + affectedRows + " ,cardNo=" + cardNo + ",busId=" + busId + ",tradeDate=" + tradeDate);
                    }


                   byte[] datRecord = DataConverter.convertToDATFormat(record , counter);
                    if(datRecord == null){ //convert fail
                        jedis.hincrBy(contextRedisKey , Constant.CONTEXT_ERROR_LINE_COUNT , 1);
                        String errorInfo = jedis.hget(contextRedisKey , Constant.CONTEXT_ERROR_INFO);
                        if("".equals(errorInfo)){
                            jedis.hset(contextRedisKey , Constant.CONTEXT_ERROR_INFO , String.valueOf(counter) );
                        }else{
                            jedis.hset(contextRedisKey , Constant.CONTEXT_ERROR_INFO , errorInfo + Constant.ERROR_INFO_SEPARATOR + String.valueOf(counter));
                        }
                        jedis.hincrBy(contextRedisKey , Constant.CONTEXT_PROGRESS_LINE_COUNT , 1);
                    }else{
                        final String dataLineRedisKey = Constant.DATEFILES_REDIS_KEY_PREFIX + record.get("line_id") + ":" + fileDateInfo;
                        jedis.sadd(dataOutlineRedisKey , dataLineRedisKey);
                        jedis.append(dataLineRedisKey , Hex.encodeHexString(datRecord) );
                        jedis.hincrBy(contextRedisKey ,Constant.CONTEXT_PROGRESS_LINE_COUNT , 1);
                    }
                    counter ++;
                }

                logger.info("---------------End with " + String.valueOf(jedis.hget(contextRedisKey  , Constant.CONTEXT_PROGRESS_LINE_COUNT)));

                in.close();

                // get dat file from redis and save it to file system
                Set<String> buslineDatSet = jedis.smembers(dataOutlineRedisKey);
                for(String buslineDatKey : buslineDatSet){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm");
                    Date now = new Date();
                    String fileName = DataConverter.generateFileName(buslineDatKey.substring(Constant.DATEFILES_REDIS_KEY_PREFIX.length() , buslineDatKey.length() - 9)  , fileDateInfo , timeFormat.format(now));
                    FileUtility.save(ContextConfig.SPLIT_DATA_DIR + fileName , Hex.decodeHex( jedis.get(buslineDatKey)) );
                }
                //store the context txt file
                //regain the content of contextMap
                contextMap = jedis.hgetAll(contextRedisKey);
                StringBuilder fileContent = new StringBuilder("");
                for(Map.Entry<String , String> entry : contextMap.entrySet()){
                    fileContent.append(entry.getKey()).append(":").append(entry.getValue()).append(System.getProperty("line.separator"));
                }
                String contextFileName = fileDateInfo + ContextConfig.CONTEXT_FILE_NAME_SUFFIX;
                FileUtility.save(ContextConfig.CONTEXT_TXT_DIR + contextFileName , fileContent.toString().getBytes());

                //clear redis
                jedis.del(contextRedisKey);
                for(String buslineDatKey : buslineDatSet){
                    jedis.del(buslineDatKey);
                }
                jedis.del(dataOutlineRedisKey);

                //clear csv file
                //todo mv csv to other dir
                FileUtility.clean(mappedBuffer);
                File renameToFile = new File(ContextConfig.TRADE_DATA_BACKUP_DIR + tradeDataFile.getName());
                if(tradeDataFile.renameTo(renameToFile) == false)
                {
                    logger.error("Move file " + tradeDataFile.getAbsolutePath() + " to " + renameToFile.getAbsolutePath() + " fail.");
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected exception , ",e);
        }

    }
}
