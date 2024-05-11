package ch.uzh.ifi.hase.soprafs24.model;

public class Response {
    private boolean success;
    private String receiptId;
    private String message;
    private boolean auth = true;

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getreceiptId() {
        return receiptId;
    }

    public void setreceiptId(String receiptId) {
        this.receiptId = receiptId;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
