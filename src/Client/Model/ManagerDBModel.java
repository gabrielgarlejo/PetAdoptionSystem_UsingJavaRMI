package Client.Model;

import Common.Pet;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ManagerDBModel {
    private SocketModel socketModel;

    public ManagerDBModel(SocketModel socketModel) {
        this.socketModel = socketModel;
    }

    public List<Pet> getPets() {
        List<Pet> pets = new ArrayList<>();
        
        try {
            socketModel.getWriter().write("<retrieve_pets_request></retrieve_pets_request>\nEND\n");
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
            pets = new ArrayList<>();

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
        return pets;
    }

    public Document addPet(String name, String breed, int age, String category, String description, String imagePath) {
        try {
            File imageFile = new File(imagePath);

            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            String imageBase64Encoded = Base64.getEncoder().encodeToString(fileContent);
            String imageName = imageFile.getName();

            String requestMessage = "<add_pet_request><pet><name>"+name+"</name><breed>"+breed+"</breed><age>"+age+"</age><category>"+category+"</category><description>"+description+"</description><image_name>"+imageName+"</image_name><image_base64_encoded>"+imageBase64Encoded+"</image_base64_encoded></pet></add_pet_request>\nEND\n";
            socketModel.getWriter().write(requestMessage);
            socketModel.getWriter().flush();

            StringBuilder responseXML = new StringBuilder();
            String line;

            // read the respones from the server
            while(((line = socketModel.getReader().readLine()) != null) && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            // convert the response to a Document
            Document responseDocument = stringToDocument(responseXML.toString());

            return responseDocument;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document deletePet(String petID) {
        try {
            socketModel.getWriter().write("<delete_pet_request><pet_id>"+petID+"</pet_id></delete_pet_request>\nEND\n");
            socketModel.getWriter().flush();

            StringBuilder responseXML = new StringBuilder();
            String line;

            // read the respones from the server
            while(((line = socketModel.getReader().readLine()) != null) && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            // convert the response to a Document
            Document responseDocument = stringToDocument(responseXML.toString());

            return responseDocument;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Pet> searchPets(String query) {
        List<Pet> matchingPets = new ArrayList<>();
        List<Pet> allPets = getPets();

        query = query.toLowerCase();
        for (Pet pet : allPets) {
            if (pet.getName().toLowerCase().contains(query) ||
                    pet.getBreed().toLowerCase().contains(query) ||
                    pet.getDescription().toLowerCase().contains(query) ||
                    pet.getCategory().toLowerCase().contains(query)) {
                matchingPets.add(pet);
            }
        }
        return matchingPets;
    }

    public Document updatePet(String id, String name, String breed, int age, String category, String description) {
        try {

            String request = "<update_pet_request><pet><pet_id>"+id+"</pet_id><name>"+name+"</name><breed>"+breed+"</breed><age>"+age+"</age><category>"+category+"</category><description>"+description+"</description></pet></update_pet_request>\nEND\n";

            socketModel.getWriter().write(request);
            socketModel.getWriter().flush();

            StringBuilder responseXML = new StringBuilder();
            String line;

            // read the respones from the server
            while(((line = socketModel.getReader().readLine()) != null) && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            // convert the response to a Document
            Document responseDocument = stringToDocument(responseXML.toString());

            return responseDocument;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void logout() {
        try {
            socketModel.getWriter().close();
            socketModel.getReader().close();
            socketModel.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getFilteredAdoptionsHistory(String startDate, String endDate) {
        StringBuilder adoptionsHistory = new StringBuilder();
        adoptionsHistory.append("------------------------------------------------------------------").append("\n");

        try {
            String filteredAdoptionHistorySring = "";

            socketModel.getWriter().write("<retrieve_adoptions_history_filtered><start_date>"+startDate+"</start_date><end_date>"+endDate+"</end_date></retrieve_adoptions_history_filtered>\nEND\n");
            socketModel.getWriter().flush();

            StringBuilder responseXML = new StringBuilder();
            String line;

            // read the respones from the server
            while(((line = socketModel.getReader().readLine()) != null) && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            filteredAdoptionHistorySring = responseXML.toString();

            Document filteredAdoptionHIstory = stringToDocument(filteredAdoptionHistorySring);
            NodeList adoptionList = filteredAdoptionHIstory.getElementsByTagName("adoption");

            // Iterate through each adoption entry
            for (int i = 0; i < adoptionList.getLength(); i++) {
                Node adoptionNode = adoptionList.item(i);
                if (adoptionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element adoptionElement = (Element) adoptionNode;

                    // adoption details
                    String adoptionId = adoptionElement.getElementsByTagName("adoption_id").item(0).getTextContent();
                    String date = adoptionElement.getElementsByTagName("adoption_date").item(0).getTextContent();

                    // adopter details
                    String firstname = adoptionElement.getElementsByTagName("firstname").item(0).getTextContent();
                    String lastname = adoptionElement.getElementsByTagName("lastname").item(0).getTextContent();
                    String adopterName = firstname + " " + lastname;
                    String petName = adoptionElement.getElementsByTagName("name").item(0).getTextContent();
                    String breed = adoptionElement.getElementsByTagName("breed").item(0).getTextContent();
                    String age = adoptionElement.getElementsByTagName("age").item(0).getTextContent();
                    String category = adoptionElement.getElementsByTagName("category").item(0).getTextContent();
                    String description = adoptionElement.getElementsByTagName("description").item(0).getTextContent();

                    adoptionsHistory.append("Adoption ID:\t").append(adoptionId).append("\n");
                    adoptionsHistory.append("Adoption Date:\t").append(date).append("\n");
                    adoptionsHistory.append("Adopter Name:\t").append(adopterName).append("\n");
                    adoptionsHistory.append("Pet Name:\t").append(petName).append("\n");
                    adoptionsHistory.append("Breed:\t\t").append(breed).append("\n");
                    adoptionsHistory.append("Age:\t\t").append(age).append("\n");
                    adoptionsHistory.append("Category:\t").append(category).append("\n");
                    adoptionsHistory.append("Description:\t").append(description).append("\n");
                    adoptionsHistory.append("------------------------------------------------------------------").append("\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return adoptionsHistory.toString();
    }

    public String getServerLogs() {
        try {
            socketModel.getWriter().write("<retrieve_logs_request></retrieve_logs_request>\nEND\n"); 
            socketModel.getWriter().flush();

            StringBuilder responseXML = new StringBuilder();
            String line;

            // read the respones from the server
            while(((line = socketModel.getReader().readLine()) != null) && !line.equals("END")) {
                responseXML.append(line).append("\n");
            }

            // convert the response to a Document
            Document responseDocument = stringToDocument(responseXML.toString());

            StringBuilder logs = new StringBuilder();
            NodeList logEntries = responseDocument.getElementsByTagName("log");

            for (int i = 0; i < logEntries.getLength(); i++) {
                Node logNode = logEntries.item(i);
                if (logNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element logElement = (Element) logNode;

                    String timestamp = logElement.getElementsByTagName("timestamp").item(0).getTextContent();
                    String description = logElement.getElementsByTagName("description").item(0).getTextContent();

                    logs.append("Timestamp: ").append(timestamp).append("\n");
                    logs.append("Description: ").append(description).append("\n");
                    logs.append("----------------------------------------\n");
                }
            }

            return logs.toString();
        } catch (IOException e) {
        }
        return "Error retrieving server logs";
    }
    
    // Utility methods
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

    static String documentToString(Document document) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
    
            // Set output properties
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Pretty print
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Set indentation spaces
    
            // Convert DOM to String
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }
}
