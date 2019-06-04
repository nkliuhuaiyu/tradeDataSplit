package dao;

import org.apache.ibatis.annotations.Mapper;
import pojo.QRTradeDataDO;

import java.util.List;
import java.util.Map;

@Mapper
public interface QRTradeDataDao {
    int updateTradeData(Map<String, Object> params);
    int backupTradeData(QRTradeDataDO qrTradeDataDO);
}
