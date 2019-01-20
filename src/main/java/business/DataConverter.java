package business;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.CRC16;

public class DataConverter {
    public static Logger logger = LogManager.getLogger(DataConverter.class);

    public static byte[] convertToDATFormat(CSVRecord csvRecord , int line){
        try {
            StringBuilder datFormatString = new StringBuilder("");

            //A/B卡区分
            datFormatString.append("05");
            //POS机交易流水号
            String udSn = csvRecord.get("ud_sn");
            datFormatString.append(generateStandardRecord(udSn , 8));
            //运营单位代码
            datFormatString.append("07000000");
            //POS机编号
            String posId = csvRecord.get("pos_id");
            String csvPosId = generateStandardRecord(posId , 12);
            datFormatString.append("5210" + csvPosId.substring(4 , 12));

            //PSAM卡号/SAM卡终端机号
            datFormatString.append("000000000000");
            //SAM卡流水号
            datFormatString.append("00000000");
            //城市代码（卡属地）
            datFormatString.append("1100");
            //卡内号user_id字段（10字节）的前8字节
            String userId = generateStandardRecord( csvRecord.get("user_id") , 20);
            datFormatString.append(userId.substring(0 , 16));
            //卡交易计数器
            datFormatString.append("0000");
            //主卡类型
            datFormatString.append("13");
            //子卡类型
            datFormatString.append("79");
            //交易前卡余额
            datFormatString.append("00000000");
            //交易金额 0010 -> 000a
            String tradeAmount = csvRecord.get("trade_amount");
            String decimalTradeAmount = generateStandardRecord(tradeAmount  , 8);
            datFormatString.append(generateStandardRecord(Integer.toHexString( Integer.valueOf(decimalTradeAmount)) , 8));
            //优惠前票价金额 0010 -> 000a
            String originalAmount = csvRecord.get("original_amount");
            String decimaloriginalAmount = generateStandardRecord(originalAmount  , 4);
            datFormatString.append(generateStandardRecord(Integer.toHexString( Integer.valueOf(decimaloriginalAmount)) , 4));
            //交易发生日期
            String tradeDate = generateStandardRecord( csvRecord.get("trade_date") , 14);
            datFormatString.append(tradeDate.substring(0,8));
            //交易发生时间
            datFormatString.append(tradeDate.substring(8 , 14));
            //交易认证码
            datFormatString.append("00000000");
            //卡内版本号/应用版本号
            datFormatString.append("0000");
            //测试标志
            datFormatString.append("00");
            //行业代码
            datFormatString.append("0001");
            //消费密钥版本号
            datFormatString.append("00");
            //卡发行商代码
            datFormatString.append("1001");
            //交易类型
            datFormatString.append("04");
            //交易子类型 ?
            datFormatString.append("50");
            //交易后卡余额
            datFormatString.append("00000000");
            //透支金额
            datFormatString.append("000000");
            //前次交易设备号/POS机编号
            datFormatString.append("000000000000");
            //前次交易PSAM卡号
            datFormatString.append("000000000000");
            //前次交易时间
            datFormatString.append("00000000000000");
            //前次交易类型标识
            datFormatString.append("00");
            //前次交易金额
            datFormatString.append("00000000");
            //前次交易计数器
            datFormatString.append("0000");
            //班次编号 ?
            String shiftId = csvRecord.get("shift_id");
            datFormatString.append(generateStandardRecord(shiftId , 2));
            //线路编号
            String lineId = csvRecord.get("line_id");
            datFormatString.append(generateStandardRecord(lineId , 6));
            //站编号
            String stationId = csvRecord.get("station_id");
            datFormatString.append(generateStandardRecord(stationId , 8));
            //车辆编号
            String busId = csvRecord.get("bus_id");
            datFormatString.append(generateStandardRecord(busId , 6));
            //司机编号
            String driverCardNo = csvRecord.get("driver_card_no");
            datFormatString.append(generateStandardRecord(driverCardNo , 10));
            //联乘优惠金额
            datFormatString.append("0000");
            //卡号余位  卡内号user_id字段（10字节）的余下2位
            datFormatString.append(userId.substring(16 , 20));
            //卡发行商余位
            datFormatString.append("0000");
            //预留
            datFormatString.append("00");

            byte[] hexDatWithoutCRC = Hex.decodeHex(datFormatString.toString());
            if(hexDatWithoutCRC.length != 126){
                logger.info("length of converted record != 126." + csvRecord.toString());
                return null;
            }
            //校验位
            int crc16 =  CRC16.CRC16( hexDatWithoutCRC , hexDatWithoutCRC.length);
            byte[] hexDat = new byte[128];
            System.arraycopy(hexDatWithoutCRC , 0 , hexDat , 0 , 126);
            hexDat[126] = (byte)(crc16 & 0xFF);
            hexDat[127] = (byte)((crc16 >> 8) & 0xFF);
            return hexDat;
        } catch (Exception e) {
            logger.info("Csv record convert fail , line number : " + String.valueOf(line), e);
            return null;
        }
    }

    public static String generateStandardRecord(String record , int length) throws Exception {
        if(record.length() > length){
            throw new Exception(new StringBuilder("Record length overflow : real ").append(record).append(" ").append(record.length()).append(" vs standard ").append(length).toString());
        }else  if(record.length() ==  length){
            return record;
        }else {// record.length() < length
            StringBuilder padding = new StringBuilder();
            for(int i = 0 ; i < length - record.length() ; i ++){
                padding.append("0");
            }
            logger.trace(new StringBuilder("Record ").append(record).append(" left padding with 0"));
            return padding.append(record).toString();
        }
    }

    public static String generateFileName(String busLine , String date , String time){
        return  Constant.SPLIT_FILE_PREFIX + busLine + Constant.POS_ID + date + time + Constant.SPLIT_FILE_SUFFIX;
    }
}
