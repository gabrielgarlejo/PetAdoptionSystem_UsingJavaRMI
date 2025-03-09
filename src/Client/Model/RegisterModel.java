package Client.Model;

import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class RegisterModel {
    private SocketModel socketModel;
    private static final String HOST_ADDRESS = "localhost";
    private static final int PORT_NUMBER = 5000;
    
    public Document registerUser(String firstName, String lastName, String email, String password, String contactNumber) {
        Document response = null;

        try {
            socketModel = new SocketModel(new Socket(HOST_ADDRESS, PORT_NUMBER));   

            String request = "<register_request><email>"+email+"</email><password>"+password+"</password><firstname>"+firstName+"</firstname><lastname>"+lastName+"</lastname><contact_number>"+contactNumber+"</contact_number></register_request>";
            request += "\nEND\n";
    
            socketModel.getWriter().write(request);
            socketModel.getWriter().flush();
            
            StringBuilder responseString = new StringBuilder();
            String line = "";

            while((line = socketModel.getReader().readLine()) != null && !line.equals("END")) {
                responseString.append(line).append("\n");
            }

            response = stringToDocument(responseString.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    // utility method
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
