package business;

public class Constant {
    public static final char LINUX_NEW_LINE = '\n';

    public static final String CONTEXT_REDIS_KEY_PREFIX = "TDS:QR:CONTEXT:";
    public static final String DATEFILE_OUTLINE_REDIS_KEY_PREFIX = "TDS:QR:BUSLINES:";
    public static final String DATEFILES_REDIS_KEY_PREFIX = "TDS:QR:";

    public static final String CONTEXT_FILENAME = "fileName";
    public static final String CONTEXT_FILELSIZE = "fileSize";
    public static final String CONTEXT_FILEMD5 = "fileMD5";
    public static final String CONTEXT_LINE_COUNT = "fileLineCount";
    public static final String CONTEXT_PROGRESS_LINE_COUNT = "progressLineCount";
    public static final String CONTEXT_ERROR_LINE_COUNT = "errorLineCount";
    public static final String CONTEXT_ERROR_INFO = "errorInfo";

    public static final String ERROR_INFO_SEPARATOR = ",";

    public static final String FILE_NAME_PREFIX = "IBUS";

    public static final String SPLIT_FILE_PREFIX = "CL";
    public static final String POS_ID = "521000000002";
    public static final String SPLIT_FILE_SUFFIX = "80.DAT";
}
