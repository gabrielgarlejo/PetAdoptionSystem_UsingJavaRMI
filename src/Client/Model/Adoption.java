package Client.Model;

import java.util.Date;

public class Adoption {
    private String adoptionId;
    private String petName;
    private String adopterName;
    private Date adoptionDate;
    private String status;

    public Adoption(String adoptionId, String petName, String adopterName, Date adoptionDate, String status) {
        this.adoptionId = adoptionId;
        this.petName = petName;
        this.adopterName = adopterName;
        this.adoptionDate = adoptionDate;
        this.status = status;
    }

    // Getters
    public String getAdoptionId() {
        return adoptionId;
    }

    public String getPetName() {
        return petName;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public Date getAdoptionDate() {
        return adoptionDate;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setAdoptionId(String adoptionId) {
        this.adoptionId = adoptionId;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public void setAdoptionDate(Date adoptionDate) {
        this.adoptionDate = adoptionDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Adoption{" +
                "adoptionId='" + adoptionId + '\'' +
                ", petName='" + petName + '\'' +
                ", adopterName='" + adopterName + '\'' +
                ", adoptionDate=" + adoptionDate +
                ", status='" + status + '\'' +
                '}';
    }
}