package Server;

import Common.Pet;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

public class Server {
    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static volatile boolean isRunning = false;
    private static ServerSocket serverSocket;
    private static ExecutorService clientPool = Executors.newCachedThreadPool();

    public static void startServer() {
        if (isRunning) {
            System.out.println("Server is already running.");
            return;
        }
        isRunning = true;
        
        // to reinitialize the clientPool when restarting the server
        clientPool = Executors.newCachedThreadPool();
        
        Thread serverThread = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(PORT)) {
                serverSocket = ss;
                System.out.println("Server started on port " + PORT);
    
                while (isRunning) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("A new client connected: " + socket);
                        ClientHandler clientHandler = new ClientHandler(socket);
                        clients.add(clientHandler);
                        clientPool.execute(clientHandler); // No more rejected tasks
                    } catch (IOException e) {
                        if (isRunning) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }
    

    public static void stopServer() {
        if (!isRunning) {
            System.out.println("Server is not running.");
            return;
        }
        isRunning = false;
        System.out.println("Shutting down server...");

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.closeResources();
            }
            clients.clear();
        }
        clientPool.shutdown();
        System.out.println("Server stopped.");
    }

    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type [start] to start the server, [stop] to stop it, and [exit] to quit the program.");
        
        while (true) {
            String command = scanner.nextLine();
            if (command.equalsIgnoreCase("start")) {
                startServer();
            } else if (command.equalsIgnoreCase("stop")) {
                stopServer();
            } else if (command.equalsIgnoreCase("exit")) {
                stopServer();
                System.out.println("Exiting program.");
                break;
            }
        }
        scanner.close();
    }
}

class ClientHandler implements Runnable {
    private Socket socket;

    final static String ADOPTER_XML_PATH = "src/xml/adopter_accounts.xml";
    final static String MANAGER_XML_PATH = "src/xml/manager_accounts.xml";
    final static String ADOPTIONS_XML_PATH = "src/xml/adoptions.xml";
    final static String PETS_XML_PATH = "src/xml/pets.xml";
    final static String LOGS_XML_PATH = "src/xml/server_logs.xml";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {

            String line;
            StringBuilder xmlData = new StringBuilder();

            while(true) {
                line = reader.readLine();

                if (line.equals("END")) {
                    handleRequest(stringToDocument(xmlData.toString()), reader, writer);

                    xmlData.setLength(0);
                } else {
                    xmlData.append(line).append("\n");
                }
            }
        } catch (IOException | NullPointerException e) {
            // e.printStackTrace();
        } finally {
            Server.removeClient(this);
            closeResources();
        }
    }

    public void closeResources() {
        try {
            System.out.println("Client disconnected: " + socket);
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A helper method that checks the 'request type' of the payload which then executes the
     * appropriate 'request handler' method
     * @param requestDocument contains the request type and the request payload
     * @param reader
     * @param writer
     */
    static void handleRequest(Document requestDocument, BufferedReader reader, BufferedWriter writer) {

        String requestDocumentRootName = requestDocument.getDocumentElement().getNodeName();

        try {
            if(requestDocumentRootName.equalsIgnoreCase("login_request")) {
                String response = loginHandler(requestDocument);
                writer.write(response+"\nEND\n");
                writer.flush();
                return;
            } 

            if(requestDocumentRootName.equalsIgnoreCase("retrieve_pets_request")) {
                String response = retrievePetsListString();
                writer.write(response+"\nEND\n");
                writer.flush();
                
                addLogEntry("Server received request: retrieve_pets_request");
                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("retrieve_adopter_account_details")) {
                String response = retrieveAdopterAccountDetails(requestDocument);
                writer.write(response+"\nEND\n");
                writer.flush();

                addLogEntry("Server received request: retrieve_adopter_account_details");
                return;
            }
            
            if(requestDocumentRootName.equalsIgnoreCase("update_account_details_request")) {
                String response = updateAdopterAccountDetails(requestDocument);
                writer.write("<update_account_details_response><message>"+response+"</message></update_account_details_response>\nEND\n");
                writer.flush();

                addLogEntry("Server received request: update_account_details_request");
                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("adopt_pet_request")) {
                String response[] = adoptPet(requestDocument);
                boolean successful = false;

                addLogEntry("Server received request: adopt_pet_request");

                if(response[0].contains("Congratulations")) {
                    successful = true;

                    addLogEntry("Someone Adopted a Pet");
                }

                writer.write("<adopt_pet_response><status>"+successful+"</status><message>"+response[0]+"</message></adopt_pet_response>\nEND\n");
                writer.flush();

                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("retrieve_adoptions_history_request")) {
                String adoptionsHistoryXML = retrieveAdoptionsHistory(requestDocument);

                writer.write("<retrieve_adoptions_history_response>"+adoptionsHistoryXML+"</retrieve_adoptions_history_response>\nEND\n");
                writer.flush();

                addLogEntry("Server received request: retrieve_adoptions_history_request");
                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("register_request")) {
                String message = registerUser(requestDocument);
                boolean success = false;

                if(message.contains("Congratulations")) {
                    success = true;
                }
                
                writer.write("<register_response><status>"+success+"</status><message>"+message+"</message></register_response>\nEND\n");
                writer.flush();

                addLogEntry("Server received request: register_request");
                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("add_pet_request")) {
                boolean success = addPet(requestDocument);
                String message = "";

                if(success) {
                    message = "Success: New Pet Registered";
                } else {
                    message = "Error: Failed to register new pet";
                }

                writer.write("<add_pet_response><status>"+success+"</status><message>"+message+"</message></add_pet_response>\nEND\n");
                writer.flush();

                addLogEntry("Server received request: add_pet_request");
                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("update_pet_request")) {
                boolean success = updatePet(requestDocument);
                String message = "";

                if(success) {
                    message = "Success: Pet infromation was updated";
                } else {
                    message = "Error: Failed to update pet information";
                }

                writer.write("<update_pet_response><status>"+success+"</status><message>"+message+"</message></update_pet_response>\nEND\n");
                writer.flush();

                addLogEntry("Server received request: add_pet_request");
                return;
            }

            if(requestDocumentRootName.equalsIgnoreCase("retrieve_adoptions_history_filtered")) {
                String startDate = requestDocument.getElementsByTagName("start_date").item(0).getTextContent();
                String endDate = requestDocument.getElementsByTagName("end_date").item(0).getTextContent();

                String response = retrieveAdoptionsHistoryFiltered(startDate, endDate);
                String finalizedResponse = "<retrieve_adoptions_history_filtered_response>"+response+"</retrieve_adoptions_history_filtered_response>\nEND\n";

                writer.write(finalizedResponse);
                writer.flush();

                addLogEntry("Server received request: retrieve_adoptions_history_filtered");
                return;
            }
            
            if(requestDocumentRootName.equalsIgnoreCase("retrieve_logs_request")) {
                String serverLogsString = retrieveServerLogs();
                String response = "<retrieve_logs_response>"+serverLogsString+"</retrieve_logs_response>\nEND\n";

                writer.write(response);
                writer.flush();

                addLogEntry("Server received request: retrieve_logs_request");
                return;
            }
            
            if(requestDocumentRootName.equalsIgnoreCase("delete_pet_request")) {
                String message = deletePet(requestDocument);
                boolean status = message.contains("Success");

                String response = "<delete_pet_response><status>"+status+"</status><message>"+message+"</message></delete_pet_response>\nEND\n";

                writer.write(response);
                writer.flush();

                addLogEntry("Server received request: Delete Pet Request");
                return;
            }

            System.out.println("--- INVALID REQUEST RECEIVED ---");
            System.out.println(documentToString(requestDocument));
            System.out.println("--------------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String loginHandler(Document requestDocument) {
        String email = requestDocument.getElementsByTagName("email").item(0).getTextContent();
        String password = requestDocument.getElementsByTagName("password").item(0).getTextContent();
        String uuid = "";
        String role = "";
        boolean status;
        String message;

        if ((uuid = checkCredentials(ADOPTER_XML_PATH, "adopter", email, password)) != null) {
            status = true;
            role = "adopter";
            message = "Login Successful";

            addLogEntry("User [" + email + "] has logged in");
        } else if((uuid = checkCredentials(MANAGER_XML_PATH, "manager", email, password)) != null) {
            status = true;
            role = "manager";
            message = "Login Successful";

            addLogEntry("Manager [" + email + "] has logged in");
        } else {
            status = false;
            role = null;
            message = "Login Unsuccessful";
        }

        return "<login_response><status>"+status+"</status><uuid>"+uuid+"</uuid><role>"+
                role+"</role><message>"+message+"</message></login_response>";
    }

    /**
     * Helper method of loginHandler
     * @param filePath
     * @param tagName adopter or manager
     * @param email
     * @param password
     * @return uuid of user if credentials are valid
     */
    static String checkCredentials(String filePath, String tagName, String email, String password) {
        try {
            File xmlFile = new File(filePath);
    
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nodeList = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String uuid = element.getElementsByTagName("uuid").item(0).getTextContent();
                    String xmlEmail = element.getElementsByTagName("email").item(0).getTextContent();
                    String xmlPassword = element.getElementsByTagName("password").item(0).getTextContent();
                    // String firstname = element.getElementsByTagName("firstname").item(0).getTextContent();
                    // String lastname = element.getElementsByTagName("lastname").item(0).getTextContent();
                    // String contactNumber = element.getElementsByTagName("contact_number").item(0).getTextContent();
    
                    if (xmlEmail.equals(email) && xmlPassword.equals(password)) {
                        return uuid;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<Pet> retrievePetsList() {
        List<Pet> pets = new ArrayList<>();
        try {
            File xmlFile = new File(PETS_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("pet");
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
                    String imageBase64Encoded = fileToBase64EncodedString(imagePath);
                    String status = element.getElementsByTagName("status").item(0).getTextContent();

                    Element imageBase64= doc.createElement("image_base64"); 
                    imageBase64.appendChild(doc.createTextNode(imageBase64Encoded));

                    element.appendChild(imageBase64);

                    Pet pet = new Pet(id, name, breed, age, category, description, imagePath, status);
                    pet.setImageBase64Encoded(imageBase64Encoded);

                    pets.add(pet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pets;
    }
    
    /**
     * @return XML formatted list of pets, with the images converted to base64 string
     */
    static String retrievePetsListString() {
        String petsListXMLFormat = "";
        try {
            File xmlFile = new File(PETS_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("pet");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String imagePath = element.getElementsByTagName("image_path").item(0).getTextContent();
                    String imageBase64Encoded = fileToBase64EncodedString(imagePath);

                    Element imageBase64= doc.createElement("image_base64"); 
                    imageBase64.appendChild(doc.createTextNode(imageBase64Encoded));

                    element.appendChild(imageBase64);
                }
            }

            petsListXMLFormat = documentToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return petsListXMLFormat;
    }

    static String retrieveAdopterAccountDetails(Document requestDocument) {
        String responseString = "";
        try {
            File xmlFile = new File(ADOPTER_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Document responseDocument = dBuilder.newDocument();
            Element rootElement = responseDocument.createElement("retrieve_adopter_account_details_response");
            responseDocument.appendChild(rootElement);

            NodeList nodeList = doc.getElementsByTagName("adopter");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element adopterElement = (Element) node;

                    String uuid = adopterElement.getElementsByTagName("uuid").item(0).getTextContent();

                    if(uuid.equals(requestDocument.getElementsByTagName("uuid").item(0).getTextContent())) {
                        Node importedNode = responseDocument.importNode(adopterElement, true);
                        rootElement.appendChild(importedNode);
                        break;
                    }
                }
            }
            responseString = documentToString(responseDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseString;
    }

    synchronized static String updateAdopterAccountDetails(Document requestDocument) {
        String responseMessage = "Error: Account was not updated";
        try {
            String uuidToUpdate = requestDocument.getElementsByTagName("uuid").item(0).getTextContent();

            File xmlFile = new File(ADOPTER_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document adoptersDocument = dBuilder.parse(xmlFile);
            adoptersDocument.getDocumentElement().normalize();

            NodeList adopterList = adoptersDocument.getElementsByTagName("adopter");
            for (int i = 0; i < adopterList.getLength(); i++) {
                Node adopterNode = adopterList.item(i);
                if (adopterNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element adopterElement = (Element) adopterNode;

                    // Check if the UUID matches
                    String uuid = adopterElement.getElementsByTagName("uuid").item(0).getTextContent();
                    if (uuid.equals(uuidToUpdate)) {
                        adopterElement.getElementsByTagName("email").item(0).setTextContent(requestDocument.getElementsByTagName("email").item(0).getTextContent());
                        adopterElement.getElementsByTagName("firstname").item(0).setTextContent(requestDocument.getElementsByTagName("firstname").item(0).getTextContent());
                        adopterElement.getElementsByTagName("lastname").item(0).setTextContent(requestDocument.getElementsByTagName("lastname").item(0).getTextContent());
                        adopterElement.getElementsByTagName("contact_number").item(0).setTextContent(requestDocument.getElementsByTagName("contact_number").item(0).getTextContent());
                        responseMessage = "Account was successfully updated";
                        System.out.println("Account was successfully updated");
                    }
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(adoptersDocument);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            saveXMLDocument(adoptersDocument, ADOPTER_XML_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMessage;
    }

    synchronized static String[] adoptPet(Document requestDocument) {
        String message = "Adoption Error: Something went wrong!";
        String[] messageArray = new String[2];
        try {
            // get the pet_id and adopter_id from requestDocument
            String petId = requestDocument.getElementsByTagName("pet_id").item(0).getTextContent();
            String adopterId = requestDocument.getElementsByTagName("uuid").item(0).getTextContent();

            File petsFile = new File(PETS_XML_PATH);
            Document petsDocument = loadXMLDocument(petsFile);
            Document userDetailsDocument = stringToDocument(retrieveAdopterAccountDetails(requestDocument));
            String adopterFirstName =userDetailsDocument.getElementsByTagName("firstname").item(0).getTextContent();
            String adopterLastName = userDetailsDocument.getElementsByTagName("lastname").item(0).getTextContent();

            // find the pet with pet_id
            NodeList petList = petsDocument.getElementsByTagName("pet");
            boolean petFound = false;
            for (int i = 0; i < petList.getLength(); i++) {
                Node petNode = petList.item(i);
                if (petNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element petElement = (Element) petNode;
                    String id = petElement.getElementsByTagName("pet_id").item(0).getTextContent();
                    String petName = petElement.getElementsByTagName("name").item(0).getTextContent();
                    String status = petElement.getElementsByTagName("status").item(0).getTextContent();

                    if (id.equals(petId)) {
                        petFound = true;
                        if (status.equals("Available")) {
                            // change the pet's status to "Unavailable"
                            petElement.getElementsByTagName("status").item(0).setTextContent("Unavailable");

                            // save the updated pets.xml file
                            saveXMLDocument(petsDocument, PETS_XML_PATH);

                            // add the adoption record to adoptions.xml
                            addAdoptionEntry(petId, adopterId);
                            messageArray[0] = "Congratulations! You have successfully adopted " + petName + "!";
                            messageArray[1]= adopterFirstName + " " + adopterLastName + " has adopted" + petName;
                        } else {
                            messageArray[0] = "Pet is already adopted";
                        }
                        break;
                    }
                }
            }

            if (!petFound) {
                messageArray[0] = "Pet ID not found";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageArray;
    }

    static String retrieveAdoptionsHistory(Document requestDocument) {
        try {
            String userUUID = requestDocument.getElementsByTagName("uuid").item(0).getTextContent();

            File adoptionsFile = new File(ADOPTIONS_XML_PATH);
            File petsFile = new File(PETS_XML_PATH);

            Document adoptionsDocument = loadXMLDocument(adoptionsFile);
            Document petsDocument = loadXMLDocument(petsFile);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document responseDocument = dBuilder.newDocument();

            Element rootElement = responseDocument.createElement("adoptions_history");
            responseDocument.appendChild(rootElement);

            NodeList adoptionList = adoptionsDocument.getElementsByTagName("adoption");
            boolean hasEntries = false;

            for (int i = 0; i < adoptionList.getLength(); i++) {
                Node adoptionNode = adoptionList.item(i);
                if (adoptionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element adoptionElement = (Element) adoptionNode;
                    String adopterId = adoptionElement.getElementsByTagName("adopter_id").item(0).getTextContent();
                    String petId = adoptionElement.getElementsByTagName("pet_id").item(0).getTextContent();

                    if (adopterId.equals(userUUID)) {
                        hasEntries = true;

                        Element adoptionRecord = responseDocument.createElement("adoption_record");

                        Node importedAdoptionNode = responseDocument.importNode(adoptionElement, true);
                        adoptionRecord.appendChild(importedAdoptionNode);

                        Element petElement = getPetDetails(petsDocument, petId, responseDocument);
                        if (petElement != null) {
                            adoptionRecord.appendChild(petElement);
                        }

                        rootElement.appendChild(adoptionRecord);
                    }
                }
            }

            // If no entries were found, add a <message> node
            if (!hasEntries) {
                Element messageElement = responseDocument.createElement("message");
                messageElement.setTextContent("No adoption history found");
                rootElement.appendChild(messageElement);
            }

            return documentToString(responseDocument);

        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to retrieve adoption history";
        }
    }

    // Helper method to retrieve pet details based on pet_id
    private static Element getPetDetails(Document petsDocument, String petId, Document responseDocument) {
        NodeList petList = petsDocument.getElementsByTagName("pet");

        for (int i = 0; i < petList.getLength(); i++) {
            Node petNode = petList.item(i);
            if (petNode.getNodeType() == Node.ELEMENT_NODE) {
                Element petElement = (Element) petNode;
                String id = petElement.getElementsByTagName("pet_id").item(0).getTextContent();

                if (id.equals(petId)) {
                    // Import pet details into response document
                    return (Element) responseDocument.importNode(petElement, true);
                }
            }
        }
        return null; // Return null if no pet is found
    }

    // Helper method to add an adoption entry to adoptions.xml
    synchronized private static void addAdoptionEntry(String petId, String adopterId) {
        try {
            // Load adoptions.xml
            File adoptionsFile = new File(ADOPTIONS_XML_PATH);
            Document adoptionsDocument = loadXMLDocument(adoptionsFile);

            // Get the root element <adoptions>
            Element root = adoptionsDocument.getDocumentElement();

            // Create a new <adoption> entry
            Element adoptionElement = adoptionsDocument.createElement("adoption");

            Element adoptionIdElement = adoptionsDocument.createElement("adoption_id");
            adoptionIdElement.appendChild(adoptionsDocument.createTextNode(UUID.randomUUID().toString()));

            Element petIdElement = adoptionsDocument.createElement("pet_id");
            petIdElement.appendChild(adoptionsDocument.createTextNode(petId));

            Element adopterIdElement = adoptionsDocument.createElement("adopter_id");
            adopterIdElement.appendChild(adoptionsDocument.createTextNode(adopterId));

            Element dateElement = adoptionsDocument.createElement("date");
            dateElement.appendChild(adoptionsDocument.createTextNode(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));

            Element statusElement = adoptionsDocument.createElement("status");
            statusElement.appendChild(adoptionsDocument.createTextNode("Completed"));

            // Append child elements to <adoption>
            adoptionElement.appendChild(adoptionIdElement);
            adoptionElement.appendChild(petIdElement);
            adoptionElement.appendChild(adopterIdElement);
            adoptionElement.appendChild(dateElement);
            adoptionElement.appendChild(statusElement);

            // Append the new adoption to <adoptions>
            root.appendChild(adoptionElement);

            // Save the updated adoptions.xml file
            saveXMLDocument(adoptionsDocument, ADOPTIONS_XML_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to load an XML file into a Document
    private static Document loadXMLDocument(File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(file);
    }

    // Helper method of updateAdopterAccountDetails to save the updated document to a file
    synchronized private static void saveXMLDocument(Document document, String filePath) {
        try {
            // Create the file output stream to write the updated XML to the file
            FileWriter writer = new FileWriter(filePath);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            writer.close(); // Close the writer after writing
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }
    
    synchronized private static String registerUser(Document requestDocument) {
        try {
            String firstName = requestDocument.getElementsByTagName("firstname").item(0).getTextContent();
            String lastName = requestDocument.getElementsByTagName("lastname").item(0).getTextContent();
            String email = requestDocument.getElementsByTagName("email").item(0).getTextContent();
            String password = requestDocument.getElementsByTagName("password").item(0).getTextContent();
            String contactNumber = requestDocument.getElementsByTagName("contact_number").item(0).getTextContent();

            File xmlFile = new File(ADOPTER_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;

            if (xmlFile.exists()) {
                doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
            } else {
                doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("adopters");
                doc.appendChild(rootElement);
            }

            // checks if email already exists
            NodeList emailsNodeList = doc.getElementsByTagName("email");
            for(int i = 0; i < emailsNodeList.getLength(); i++) {
                Node node = emailsNodeList.item(i);

                // compare emails
                if(email.equalsIgnoreCase(node.getTextContent())) {
                    return "Register error: Email already exists";
                }
            }

            Element root = doc.getDocumentElement();
            Element newAdopter = doc.createElement("adopter");

            // Add adopter details to the XML
            newAdopter.appendChild(createElement(doc, "uuid", UUID.randomUUID().toString()));
            newAdopter.appendChild(createElement(doc, "email", email));
            newAdopter.appendChild(createElement(doc, "password", password));
            newAdopter.appendChild(createElement(doc, "firstname", firstName));
            newAdopter.appendChild(createElement(doc, "lastname", lastName));
            newAdopter.appendChild(createElement(doc, "contact_number", contactNumber));

            root.appendChild(newAdopter);

            // Write the updated XML back to the file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");   
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return "Congratulations! Your account has been registered";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Something went wrong: Failed to register your account!";
    }

    private static Element createElement(Document doc, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        return element;
    }
    
    synchronized private static boolean addPet(Document requestDocument) {
        try {
            String name = requestDocument.getElementsByTagName("name").item(0).getTextContent();
            String breed = requestDocument.getElementsByTagName("breed").item(0).getTextContent();
            String age = requestDocument.getElementsByTagName("age").item(0).getTextContent();
            String category = requestDocument.getElementsByTagName("category").item(0).getTextContent();
            String description = requestDocument.getElementsByTagName("description").item(0).getTextContent();
            String imageName = requestDocument.getElementsByTagName("image_name").item(0).getTextContent();
            String base64EncodedImage = requestDocument.getElementsByTagName("image_base64_encoded").item(0).getTextContent();

            File xmlFile = new File(PETS_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;

            if (xmlFile.exists()) {
                doc = dBuilder.parse(xmlFile);
            } else {
                doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("pets");
                doc.appendChild(rootElement);
            }

            Element petElement = doc.createElement("pet");

            Element idElement = doc.createElement("pet_id");
            idElement.appendChild(doc.createTextNode(UUID.randomUUID().toString())); // Generate a unique ID
            petElement.appendChild(idElement);

            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(name));
            petElement.appendChild(nameElement);

            Element breedElement = doc.createElement("breed");
            breedElement.appendChild(doc.createTextNode(breed));
            petElement.appendChild(breedElement);

            Element ageElement = doc.createElement("age");
            ageElement.appendChild(doc.createTextNode(String.valueOf(age)));
            petElement.appendChild(ageElement);

            Element categoryElement = doc.createElement("category");
            categoryElement.appendChild(doc.createTextNode(category));
            petElement.appendChild(categoryElement);

            Element descriptionElement = doc.createElement("description");
            descriptionElement.appendChild(doc.createTextNode(description));
            petElement.appendChild(descriptionElement);

            // save the image
            String imagePath = "src/res/"+imageName;

            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedImage);
            File outputFile = new File(imagePath);

            // Check if file exists and modify the filename if necessary
            // If the file already exists, it renames it by appending _1, _2, etc.
            if (outputFile.exists()) {
                String baseName = imagePath.substring(0, imagePath.lastIndexOf('.'));
                String extension = imagePath.substring(imagePath.lastIndexOf('.'));
                int count = 1;

                while (outputFile.exists()) {
                    outputFile = new File(baseName + "_" + count + extension);
                    count++;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(decodedBytes);
            }
            
            // add the finalized image path
            Element imagePathElement = doc.createElement("image_path");
            imagePathElement.appendChild(doc.createTextNode(imagePath));
            petElement.appendChild(imagePathElement);

            Element statusElement = doc.createElement("status");
            statusElement.appendChild(doc.createTextNode("Available"));
            petElement.appendChild(statusElement);

            doc.getDocumentElement().appendChild(petElement);

            // Set up the transformer to format the XML output
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Set output properties for indentation
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized static boolean updatePet(Document requestDocument) {
        String id = requestDocument.getElementsByTagName("pet_id").item(0).getTextContent();
        String name = requestDocument.getElementsByTagName("name").item(0).getTextContent();
        String breed = requestDocument.getElementsByTagName("breed").item(0).getTextContent();
        int age = Integer.parseInt(requestDocument.getElementsByTagName("age").item(0).getTextContent());
        String category = requestDocument.getElementsByTagName("category").item(0).getTextContent();
        String description = requestDocument.getElementsByTagName("description").item(0).getTextContent();

        try {
            File xmlFile = new File(PETS_XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList petList = doc.getElementsByTagName("pet");
            for (int i = 0; i < petList.getLength(); i++) {
                Element petElement = (Element) petList.item(i);
                if (petElement.getElementsByTagName("pet_id").item(0).getTextContent().equals(id)) {
                    petElement.getElementsByTagName("name").item(0).setTextContent(name);
                    petElement.getElementsByTagName("breed").item(0).setTextContent(breed);
                    petElement.getElementsByTagName("age").item(0).setTextContent(String.valueOf(age));
                    petElement.getElementsByTagName("category").item(0).setTextContent(category);
                    petElement.getElementsByTagName("description").item(0).setTextContent(description);
                    break;
                }
            }

            // Save changes back to the XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static String retrieveAdoptionsHistoryFiltered(String startDate, String endDate) {
        try {
            Document adoptionsDoc = parseXML(new File(ADOPTIONS_XML_PATH));
            Document adoptersDoc = parseXML(new File(ADOPTER_XML_PATH));
            Document petsDoc = parseXML(new File(PETS_XML_PATH));

            // Parse the date range
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // Create the output document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document resultDoc = docBuilder.newDocument();
            
            // Create root element
            Element rootElement = resultDoc.createElement("filtered_adoptions");
            resultDoc.appendChild(rootElement);

            // Iterate through adoptions and filter based on date
            NodeList adoptionList = adoptionsDoc.getElementsByTagName("adoption");
            for (int i = 0; i < adoptionList.getLength(); i++) {
                Node adoptionNode = adoptionList.item(i);
                if (adoptionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element adoptionElement = (Element) adoptionNode;
                    String adoptionDateStr = adoptionElement.getElementsByTagName("date").item(0).getTextContent();
                    Date adoptionDate = sdf.parse(adoptionDateStr);

                    // If adoption date falls within the range, process the adoption
                    if (!adoptionDate.before(start) && !adoptionDate.after(end)) {
                        // Get adoption details
                        String adoptionId = adoptionElement.getElementsByTagName("adoption_id").item(0).getTextContent();
                        String petId = adoptionElement.getElementsByTagName("pet_id").item(0).getTextContent();
                        String adopterId = adoptionElement.getElementsByTagName("adopter_id").item(0).getTextContent();
                        
                        // Get adopter details
                        String adopterEmail = getAdopterDetail(adoptersDoc, adopterId, "email");
                        String adopterFirstName = getAdopterDetail(adoptersDoc, adopterId, "firstname");
                        String adopterLastName = getAdopterDetail(adoptersDoc, adopterId, "lastname");
                        String adopterContactNumber = getAdopterDetail(adoptersDoc, adopterId, "contact_number");

                        // Get pet details
                        String petName = getPetDetail(petsDoc, petId, "name");
                        String petBreed = getPetDetail(petsDoc, petId, "breed");
                        String petCategory = getPetDetail(petsDoc, petId, "category");
                        String petDescription = getPetDetail(petsDoc, petId, "description");
                        String petAge = getPetDetail(petsDoc, petId, "age");
                        
                        // Create filtered adoption element
                        Element adoptionElementFiltered = resultDoc.createElement("adoption");
                        rootElement.appendChild(adoptionElementFiltered);

                        // Add adoption details
                        addElement(resultDoc, adoptionElementFiltered, "adoption_id", adoptionId);
                        addElement(resultDoc, adoptionElementFiltered, "pet_id", petId);
                        addElement(resultDoc, adoptionElementFiltered, "adopter_id", adopterId);
                        addElement(resultDoc, adoptionElementFiltered, "adoption_date", adoptionDateStr);

                        // Add adopter details
                        Element adopterElement = resultDoc.createElement("adopter");
                        adoptionElementFiltered.appendChild(adopterElement);
                        addElement(resultDoc, adopterElement, "email", adopterEmail);
                        addElement(resultDoc, adopterElement, "firstname", adopterFirstName);
                        addElement(resultDoc, adopterElement, "lastname", adopterLastName);
                        addElement(resultDoc, adopterElement, "contact_number", adopterContactNumber);

                        // Add pet details
                        Element petElement = resultDoc.createElement("pet");
                        adoptionElementFiltered.appendChild(petElement);
                        addElement(resultDoc, petElement, "name", petName);
                        addElement(resultDoc, petElement, "breed", petBreed);
                        addElement(resultDoc, petElement, "category", petCategory);
                        addElement(resultDoc, petElement, "description", petDescription);
                        addElement(resultDoc, petElement, "age", petAge);
                    }
                }
            }
        
            return documentToString(resultDoc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String retrieveServerLogs() {
        try {
            File logFile = new File(LOGS_XML_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(logFile);

            doc.getDocumentElement().normalize();

            return documentToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error reading logs.";
    }

    synchronized  static String deletePet(Document requestDocument) {
        try {
            String petUUID = requestDocument.getElementsByTagName("pet_id").item(0).getTextContent();

            // Parse the XML file
            File petsFile = new File(PETS_XML_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(petsFile);

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Get all <pet> elements
            NodeList petList = doc.getElementsByTagName("pet");

            boolean petDeleted = false;

            // Iterate through all <pet> elements
            for (int i = 0; i < petList.getLength(); i++) {
                Node petNode = petList.item(i);
                if (petNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element petElement = (Element) petNode;
                    String petId = petElement.getElementsByTagName("pet_id").item(0).getTextContent();

                    // If pet_id matches, delete the node
                    if (petId.equals(petUUID)) {
                        petElement.getParentNode().removeChild(petElement);
                        petDeleted = true;
                        break; // Exit after deleting the first match
                    }
                }
            }

            if (!petDeleted) {
                return ("Pet with UUID " + petUUID + " not found.");
            }

            // Save
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(PETS_XML_PATH));
            transformer.transform(source, result);

            return ("Success: Pet with UUID " + petUUID + " has been deleted.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Something went wrong: Pet was not deleted";
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
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // Set indentation spaces
    
            // Convert DOM to String
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    static String fileToBase64EncodedString(String imagePath) {
        try {
            File file = new File(imagePath);
            byte[] fileContent = Files.readAllBytes(file.toPath()); // Read file as bytes
            return Base64.getEncoder().encodeToString(fileContent); // Convert to Base64
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null in case of an error
        }
    }

    static boolean convertBase64ToImage(String base64String, String outputPath) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            File outputFile = new File(outputPath);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(decodedBytes);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Document parseXML(File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(file);
    }

    private static String getAdopterDetail(Document doc, String adopterId, String tagName) {
        NodeList adopterList = doc.getElementsByTagName("adopter");
        for (int i = 0; i < adopterList.getLength(); i++) {
            Element adopterElement = (Element) adopterList.item(i);
            String uuid = adopterElement.getElementsByTagName("uuid").item(0).getTextContent();
            if (uuid.equals(adopterId)) {
                return adopterElement.getElementsByTagName(tagName).item(0).getTextContent();
            }
        }
        return "";
    }

    private static String getPetDetail(Document doc, String petId, String tagName) {
        NodeList petList = doc.getElementsByTagName("pet");
        for (int i = 0; i < petList.getLength(); i++) {
            Element petElement = (Element) petList.item(i);
            String id = petElement.getElementsByTagName("pet_id").item(0).getTextContent();
            if (id.equals(petId)) {
                return petElement.getElementsByTagName(tagName).item(0).getTextContent();
            }
        }
        return "";
    }

    private static void addElement(Document doc, Element parentElement, String tagName, String value) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(value));
        parentElement.appendChild(element);
    }

    private synchronized static void addLogEntry(String description) {
        try {
            // Parse the existing XML file
            File logFile = new File(LOGS_XML_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(logFile);

            // Create the new log entry
            Element logEntry = doc.createElement("log");

            // Add timestamp to the log entry
            String timestamp = getCurrentTimestamp();
            Element timestampElement = doc.createElement("timestamp");
            timestampElement.appendChild(doc.createTextNode(timestamp));
            logEntry.appendChild(timestampElement);

            // Add description to the log entry
            Element descriptionElement = doc.createElement("description");
            descriptionElement.appendChild(doc.createTextNode(description));
            logEntry.appendChild(descriptionElement);

            // Add the new log entry to the logs
            NodeList logs = doc.getElementsByTagName("logs");
            if (logs.getLength() > 0) {
                Element logsElement = (Element) logs.item(0);
                logsElement.appendChild(logEntry);
            }

            // Save the updated XML file
            saveXMLToFile(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private synchronized static void saveXMLToFile(Document doc) throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(LOGS_XML_PATH));
        transformer.transform(source, result);
    }
}
