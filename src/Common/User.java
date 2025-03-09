package Common;

public class User {
    String uuid;
    String email;
    String password;
    String firstname;
    String lastname;
    String contactNumber;
    String role; // adopter or manager

    public String getUUID() {
        return uuid;
    }
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
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
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public String getContactNumber() {
        return contactNumber;
    }
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public User() {

    }

    public User(String uuid, String email, String password) {
        this.uuid = uuid;
        this.email = email;
        this.password = password;
    }

    public User(String uuid, String email, String password, String role) {
        this.uuid = uuid;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String uuid, String email, String password, String firstname, String lastname, String contactNumber) {
        this.uuid = uuid;
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.contactNumber = contactNumber;
    }

    public User(String uuid, String email, String password, String firstname, String lastname, String contactNumber,
            String role) {
        this.uuid = uuid;
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.contactNumber = contactNumber;
        this.role = role;
    }
}
