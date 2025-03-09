package Client.Model;

public class Pet {
    private String id;
    private String name;
    private String breed;
    private int age;
    private String category; // e.g., "Dogs", "Cats"
    private String description;
    private String imagePath; // Path to the pet's image
    private String status; // e.g., "Available", "Adopted"

    // Constructor
    public Pet(String id, String name, String breed, int age, String category, String description, String imagePath, String status) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.category = category;
        this.description = description;
        this.imagePath = imagePath;
        this.status = status;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for breed
    public String getBreed() {
        return breed;
    }

    // Getter for age
    public int getAge() {
        return age;
    }

    // Getter for category
    public String getCategory() {
        return category;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Getter for imagePath
    public String getImagePath() {
        return imagePath; // This method retrieves the image path
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Setter for id
    public void setId(String id) {
        this.id = id;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Setter for breed
    public void setBreed(String breed) {
        this.breed = breed;
    }

    // Setter for age
    public void setAge(int age) {
        this.age = age;
    }

    // Setter for category
    public void setCategory(String category) {
        this.category = category;
    }

    // Setter for description
    public void setDescription(String description) {
        this.description = description;
    }

    // Setter for imagePath
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Setter for status
    public void setStatus(String status) {
        this.status = status;
    }

    // toString method for easy debugging and logging
    @Override
    public String toString() {
        return "Pet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", breed='" + breed + '\'' +
                ", age=" + age +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}