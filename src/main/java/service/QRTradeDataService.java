package service;

import dao.QRTradeDataDao;
import org.apache.commons.csv.CSVRecord;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pojo.QRTradeDataDO;
import utilities.DBManager;

import java.util.HashMap;
import java.util.Map;

public class QRTradeDataService {

    public static Logger logger = LogManager.getLogger(QRTradeDataService.class);

    public static int updateTradeData(String lineId , String busId , String posId , String fileName , short status){
        SqlSession sqlSession = DBManager.sqlSessionFactory.openSession();
        try{
            QRTradeDataDao qRTradeDataDao = sqlSession.getMapper(QRTradeDataDao.class);
            Map<String , Object> params = new HashMap<String, Object>();
            params.put("lineId" , lineId);
            params.put("busId" , busId);
            params.put("posId" , posId);
            params.put("fileName" , fileName);
            params.put("status" , status);
            int affectedRows = qRTradeDataDao.updateTradeData(params);
            sqlSession.commit();
            return affectedRows;
        }finally {
            sqlSession.close();
        }
    }
    public static int insertTradeDate(CSVRecord record){
        SqlSession sqlSession = DBManager.sqlSessionFactory.openSession();
        try{
            QRTradeDataDao qRTradeDataDao = sqlSession.getMapper(QRTradeDataDao.class);
            QRTradeDataDO qrTradeDataDO = new QRTradeDataDO();
            qrTradeDataDO.setTradeNumber(record.get("trade_number"));
            qrTradeDataDO.setLineDirection(record.get("line_direction"));
            qrTradeDataDO.setBalanceDate(record.get("balance_date"));
            qrTradeDataDO.setCardNo(record.get("card_no"));
            qrTradeDataDO.setStationId(record.get("station_id"));
            qrTradeDataDO.setTradeType(record.get("trade_type"));
            qrTradeDataDO.setSegmentationFlag(record.get("segmentation_flag"));
            qrTradeDataDO.setPosId(record.get("pos_id"));
            qrTradeDataDO.setLineId(record.get("line_id"));
            qrTradeDataDO.setBusId(record.get("bus_id"));
            qrTradeDataDO.setTradeDate(record.get("trade_date"));
            qrTradeDataDO.setUdSn(record.get("ud_sn"));
            qrTradeDataDO.setTradeAmount(record.get("trade_amount"));
            qrTradeDataDO.setOriginalAmount(record.get("original_amount"));
            qrTradeDataDO.setDriverCardNo(record.get("driver_card_no"));
            qrTradeDataDO.setAttendantCardNo(record.get("attendant_card_no"));
            qrTradeDataDO.setRawData(record.get("raw_data"));
            qrTradeDataDO.setUserId(record.get("user_id"));
            qrTradeDataDO.setPayState(record.get("pay_state"));
            qrTradeDataDO.setPayType(record.get("pay_type"));
            qrTradeDataDO.setUpdateTime(record.get("update_time"));
            qrTradeDataDO.setTransactionId(record.get("transaction_id"));
            qrTradeDataDO.setCardIssuerId(record.get("card_issuer_id"));
            qrTradeDataDO.setQrcreatorcode(record.get("qrCreatorCode"));
            qrTradeDataDO.setLineName(record.get("line_name"));
            qrTradeDataDO.setShiftId(record.get("shift_id"));
            int affectedRorws = qRTradeDataDao.backupTradeData(qrTradeDataDO);
            sqlSession.commit();
            return affectedRorws;
        }catch(Exception e){
            logger.error("Insert csv record into DB fail." , e);
            return 0;
        }
        finally {
            sqlSession.close();
        }
    }
}
