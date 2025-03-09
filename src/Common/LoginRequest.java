package Common;

import java.io.Serializable;

public class LoginRequest implements Serializable{
    String email;
    String password;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public LoginRequest() {
        this.email = "";
        this.password = "";
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
