package utilities;

import business.Constant;
import business.FileContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;



public class FileUtility {

    private static Logger logger = LogManager.getLogger(FileUtility.class);

    public static List<File> getSortedFileList(String path) throws Exception {
        File filePath = new File(path);
        if(!filePath.isDirectory()){
            String errorInfo = new StringBuilder("").append(path).append(" is not a directory.").toString();
            logger.error(errorInfo);
            throw new IllegalArgumentException(errorInfo);
        }
        List<File> filesList = Arrays.asList(filePath.listFiles());
        // 按照文件修改时间从新到旧排序，最新修改的文件排在前面
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(o1.lastModified() < o2.lastModified()){
                    return 1;
                }else if (o1.lastModified() == o2.lastModified()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });
        return filesList;
    }

    public static FileContext generateFileContext(String fileName , long fileLength , MappedByteBuffer mappedBuffer) throws IOException {
        logger.trace("Start to generate File Context.");
        logger.trace("--start generate md5 digest");
        DigestUtils md5Utils = new DigestUtils("MD5");
        String md5HexInfo = md5Utils.digestAsHex(mappedBuffer);
        logger.trace(new StringBuilder("--md5 digest ").append(md5HexInfo).toString());

        logger.trace("--start compute file lines");
        int fileLines = computeLines(mappedBuffer , (int)fileLength);
        logger.trace(new StringBuilder("--file lines ").append(fileLines).toString());

        logger.trace("End of generating File Context.");
        return new FileContext(fileName , (int)fileLength , md5HexInfo ,fileLines , 0 ,0 ,"");
    }

    public static int computeLines(MappedByteBuffer byteBuffer , int size){
        int lineCount = 0;
        for(int index = 0 ; index < size ; index ++){
            Byte fileByte = byteBuffer.get(index);
            if(fileByte.equals((byte)Constant.LINUX_NEW_LINE)){
                lineCount ++;
            }
        }
        if(size > 0){
            lineCount ++;
        }
        return lineCount;
    }

    public static void save(String absolutePath , byte[] content) throws IOException {
        RandomAccessFile targetFile = new RandomAccessFile(absolutePath , "rw");
        MappedByteBuffer mappedBuffer = targetFile.getChannel().map(FileChannel.MapMode.READ_WRITE , 0 , content.length);

        mappedBuffer.put(content);

        targetFile.getChannel().close();
        targetFile.close();
    }

    //find from csdn ,must clean the mappedByteBuffer before delete the mapped file,otherwise it will delete fail.
    public static void clean(final Object buffer) throws Exception {
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod("cleaner",new Class[0]);
                    getCleanerMethod.setAccessible(true);
                    sun.misc.Cleaner cleaner =(sun.misc.Cleaner)getCleanerMethod.invoke(buffer,new Object[0]);
                    cleaner.clean();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return null;}});

    }
}
