package io.infolayer.aida;

public class Heartbeat {

    public static final int SERVICE_STARTING = 10;
    public static final int SERVICE_STARTED  = 12;
    public static final int SERVICE_STOPPING = 14;
    public static final int SERVICE_STATUS   = 16;

    public static final int TASK_SUBMITTED   = 30;
    public static final int TASK_SUCCESS     = 32;

    private String executorName;
    private String executorVersion;
    private String executorBuildDate;
    private int status;
    private String transactionId;
    private String message;

    public Heartbeat() { }

    public Heartbeat(String message) {
        this.message = message;
    }

    public Heartbeat(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public Heartbeat(int status, String message, String transactionId) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
    }

    public String getExecutorBuildDate() {
        return executorBuildDate;
    }

    public void setExecutorBuildDate(String executorBuildDate) {
        this.executorBuildDate = executorBuildDate;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getExecutorVersion() {
        return executorVersion;
    }

    public void setExecutorVersion(String executorVersion) {
        this.executorVersion = executorVersion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

}
