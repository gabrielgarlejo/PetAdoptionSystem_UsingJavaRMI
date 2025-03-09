package Client.Model;

import Common.AdopterAccount;
import Common.Pet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DashboardModel {

    public static final int IMAGE_WIDTH = 150;
    public static final int IMAGE_HEIGHT = 150;
    private SocketModel socketModel;
    private String userUUID;
    private AdopterAccount adopterAccount;
    private List<Pet> pets;

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    public void setAdopterAccount(AdopterAccount adopterAccount) {
        this.adopterAccount = adopterAccount;
    }

    public DashboardModel(SocketModel socketModel, String userUUID) {
        this.socketModel = socketModel;
        this.userUUID = userUUID;
    }

    public AdopterAccount getAdopterAccount() {
        return adopterAccount;
    }

    // TODO: retrieve list of pets from the server
    public void retrievePetsList() {
        try {
            // send the request to the server
            socketModel.getWriter().write("<retrieve_pets_request><uuid>"+userUUID+"</uuid></retrieve_pets_request>\nEND\n");
            socketModel.getWriter().flush();
            
            StringBuilder responseXML = new StringBuilder();
            String line;

            // read the respones from the server
            while(((line = socketModel.getReader().readLine()) != null) && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            // convert the response to a Document
            Document petsDocument = stringToDocument(responseXML.toString());

            // to initialize / clear the content
            this.pets = new ArrayList<>();

            // convert the document to list
            NodeList nodeList = petsDocument.getElementsByTagName("pet");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getElementsByTagName("pet_id").item(0).getTextContent();
                    String name = element.getElementsByTagName("name").item(0).getTextContent();
                    String breed = element.getElementsByTagName("breed").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                    String category = element.getElementsByTagName("category").item(0).getTextContent();
                    String description = element.getElementsByTagName("description").item(0).getTextContent();
                    String imagePath = element.getElementsByTagName("image_path").item(0).getTextContent();
                    String imageBase64 = element.getElementsByTagName("image_base64").item(0).getTextContent();
                    String status = element.getElementsByTagName("status").item(0).getTextContent();

                    Pet pet = new Pet(id, name, breed, age, category, description, imagePath, status); 
                    pet.setImageBase64Encoded(imageBase64);

                    pets.add(pet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Pet> searchPets(String searchText) {
        List<Pet> matchingPets = new ArrayList<>();

        for (Pet pet : pets) {
            if (pet.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    pet.getDescription().toLowerCase().contains(searchText.toLowerCase())) {
                matchingPets.add(pet);
            }
        }
        return matchingPets;
    }

    public List<Pet> searchPetsByText(String searchText) {
        List<Pet> matchingPets = new ArrayList<>();
        for (Pet pet : pets) {
            if (pet.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    pet.getDescription().toLowerCase().contains(searchText.toLowerCase())) {
                matchingPets.add(pet);
            }
        }
        return matchingPets;
    }

    private ImageIcon resizeImage(String base64String) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            
            // Convert byte array to BufferedImage
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            // Load the original image
            BufferedImage originalImage = ImageIO.read(byteArrayInputStream);

            // Create a new buffered image with the desired dimensions
            BufferedImage resizedImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);

            // Calculate the aspect ratio
            double originalAspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
            double targetAspectRatio = (double) IMAGE_WIDTH / IMAGE_HEIGHT;

            // Determine the scaling factor
            int newWidth, newHeight;
            if (originalAspectRatio > targetAspectRatio) {
                // Image is wider than target aspect ratio
                newWidth = IMAGE_WIDTH;
                newHeight = (int) (IMAGE_WIDTH / originalAspectRatio);
            } else {
                // Image is taller than target aspect ratio
                newHeight = IMAGE_HEIGHT;
                newWidth = (int) (IMAGE_HEIGHT * originalAspectRatio);
            }

            // Resize the original image
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            // Draw the scaled image onto the new buffered image
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null if the image cannot be loaded
        }
    }

    // Add this method to DashboardModel.java
    public AdopterAccount retrieveAdopterDetails(String userUUID) {
        AdopterAccount retrievedAdopterAccount = null;
        try {
            // send the request to retrieve user details
            socketModel.getWriter().write("<retrieve_adopter_account_details><uuid>"+userUUID+"</uuid></retrieve_adopter_account_details>\nEND\n");
            socketModel.getWriter().flush();

            StringBuilder response = new StringBuilder();
            String line = "";

            while((line = socketModel.getReader().readLine()) != null && !line.equals("END")) {
                response.append(line).append("\n");
            }

            Document responseDocument = stringToDocument(response.toString());

            // System.out.println(response.toString()); // view the response from the server

            String email = responseDocument.getElementsByTagName("email").item(0).getTextContent();
            String password = responseDocument.getElementsByTagName("password").item(0).getTextContent();
            String firstname = responseDocument.getElementsByTagName("firstname").item(0).getTextContent();
            String lastname = responseDocument.getElementsByTagName("lastname").item(0).getTextContent();
            String contactNumber = responseDocument.getElementsByTagName("contact_number").item(0).getTextContent();

            retrievedAdopterAccount = new AdopterAccount(userUUID, email, password, firstname, lastname, contactNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retrievedAdopterAccount;
    }

    public String retrieveAdoptionsHistory() {
        try {
            // send the request to retrieve user details
            socketModel.getWriter().write("<retrieve_adoptions_history_request><uuid>"+userUUID+"</uuid></retrieve_adoptions_history_request>\nEND\n");
            socketModel.getWriter().flush();
            
            StringBuilder responseXML = new StringBuilder();
            String line = "";

            while((line = socketModel.getReader().readLine()) != null && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            Document adoptionsHistoryElement = stringToDocument(responseXML.toString());

            StringBuilder historyString = new StringBuilder();
            historyString.append("\n");
    
            // Get all <adoption> nodes
            NodeList adoptionList = adoptionsHistoryElement.getElementsByTagName("adoption_record");
    
            for (int i = 0; i < adoptionList.getLength(); i++) {
                Node adoptionNode = adoptionList.item(i);
                if (adoptionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element adoptionRecordElement = (Element) adoptionNode;
    
                    // Extract data from each adoption
                    String date = adoptionRecordElement.getElementsByTagName("date").item(0).getTextContent();
                    String name = adoptionRecordElement.getElementsByTagName("name").item(0).getTextContent();
                    String category = adoptionRecordElement.getElementsByTagName("category").item(0).getTextContent();
                    String breed = adoptionRecordElement.getElementsByTagName("breed").item(0).getTextContent();
                    String description = adoptionRecordElement.getElementsByTagName("description").item(0).getTextContent();
                    String age = adoptionRecordElement.getElementsByTagName("age").item(0).getTextContent();
    
                    historyString.append("Adopted On: ").append(date).append("\n");
                    historyString.append("Pet Name: ").append(name).append("\n");
                    historyString.append("Category: ").append(category).append("\n");
                    historyString.append("Breed: ").append(breed).append("\n");
                    historyString.append("Age: ").append(age).append("\n");
                    historyString.append("Description: ").append(description).append("\n");
                    historyString.append("----------------").append("\n");
                }
            }

            return historyString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "\nNo entries were found";
    }

    public void logout() throws IOException {
        socketModel.close();
    }

    public String updateAccountDetails(String userUUID, String email, String firstName, String lastName, String contactNumber) {
        try {            
            StringBuilder requestString = new StringBuilder();
            StringBuilder response = new StringBuilder();

            requestString.append("<update_account_details_request>");
            requestString.append("<adopter>");
            requestString.append("<uuid>").append(userUUID).append("</uuid>");
            requestString.append("<email>").append(email).append("</email>");
            requestString.append("<firstname>").append(firstName).append("</firstname>");
            requestString.append("<lastname>").append(lastName).append("</lastname>");
            requestString.append("<contact_number>").append(contactNumber).append("</contact_number>");
            requestString.append("</adopter>");
            requestString.append("</update_account_details_request>").append("\n");
            requestString.append("END").append("\n");

            socketModel.getWriter().write(requestString.toString());
            socketModel.getWriter().flush();

            String line = "";
            while((line = socketModel.getReader().readLine()) != null && !line.equals("END")) {
                response.append(line).append("\n");
            }

            Document responseDocument = stringToDocument(response.toString());
            return responseDocument.getElementsByTagName("message").item(0).getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update account details", e);
        }
    }

    static Document stringToDocument(String xmlData) {
        try {
            // Use StringReader with InputSource
            InputSource inputSource = new InputSource(new StringReader(xmlData));

            // Create DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse and return Document
            return builder.parse(inputSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static ImageIcon base64ToImageIcon(String base64String) {
        try {
            // Decode Base64 string to byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            
            // Convert byte array to BufferedImage
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
            
            // Create ImageIcon from the BufferedImage
            return new ImageIcon(bufferedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null in case of an error
        }
    }
}
