package business;

public class FileContext {
    private String fileName;
    private int fileSize;
    private String fileMD5;
    private int fileTotalLines;
    private int convertedLines;
    private int errorLines;
    private String errorInfo;

    public FileContext(String fileName, int fileSize, String fileMD5, int fileTotalLines, int convertedLines, int errorLines, String errorInfo) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileMD5 = fileMD5;
        this.fileTotalLines = fileTotalLines;
        this.convertedLines = convertedLines;
        this.errorLines = errorLines;
        this.errorInfo = errorInfo;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public int getFileTotalLines() {
        return fileTotalLines;
    }

    public void setFileTotalLines(int fileTotalLines) {
        this.fileTotalLines = fileTotalLines;
    }

    public int getConvertedLines() {
        return convertedLines;
    }

    public void setConvertedLines(int convertedLines) {
        this.convertedLines = convertedLines;
    }

    public int getErrorLines() {
        return errorLines;
    }

    public void setErrorLines(int errorLines) {
        this.errorLines = errorLines;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
