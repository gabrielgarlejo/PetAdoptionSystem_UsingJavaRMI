package Client.Controller;

import Client.Model.SocketModel;
import Client.View.PetDetailsDialog;
import Common.Pet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class PetDetailsDialogController {
    private PetDetailsDialog view;
    private Pet pet;
    private String userUUID;
    private SocketModel socketModel;

    public PetDetailsDialogController(PetDetailsDialog view, Pet pet, String userUUID, SocketModel socketModel) {
        this.view = view;
        this.pet = pet;
        this.userUUID = userUUID;
        this.socketModel = socketModel;

        // Add action listener for adopt button
        this.view.addAdoptListener(new AdoptListener());
    }

    class AdoptListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Update pet status in XML
                Document responseDocument = adoptPet();
                
                boolean success = responseDocument.getElementsByTagName("status").item(0).getTextContent().equalsIgnoreCase("true");
                String message = responseDocument.getElementsByTagName("message").item(0).getTextContent();

                if(success) {
                    // Update the pet object
                    pet.setStatus("Unavailable");
    
                    // Update the dialog
                    view.updateStatus("Unavailable");
    
                    // Disable adopt button
                    view.disableAdoptButton();
                }

                view.showMessage(message);
                
            } catch (Exception ex) {
                view.showMessage("Error occurred while adopting: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private Document adoptPet() throws Exception {
        StringBuilder response = new StringBuilder();

        socketModel.getWriter().write("<adopt_pet_request><uuid>"+userUUID+"</uuid><pet_id>"+pet.getId()+"</pet_id></adopt_pet_request>\nEND\n");
        socketModel.getWriter().flush();

        String line = "";

        while((line = socketModel.getReader().readLine()) != null && !line.equals("END")) {
            response.append(line).append("\n");
        }
        
        return stringToDocument(response.toString());
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
}