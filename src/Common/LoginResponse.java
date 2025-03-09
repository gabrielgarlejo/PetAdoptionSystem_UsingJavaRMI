package Common;

import java.io.Serializable;

public class LoginResponse implements Serializable{
    private boolean status;
    private String message;
    private User userInfo;

    public boolean getStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public User getUserInfo() {
        return userInfo;
    }
    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }

    public LoginResponse() {
        
    }
    
    public LoginResponse(boolean status, String message, User userInfo) {
        this.status = status;
        this.message = message;
        this.userInfo = userInfo;
    }       
}
