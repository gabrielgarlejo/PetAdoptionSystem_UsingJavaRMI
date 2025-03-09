package Client.Model;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class LoginModel {
    private SocketModel socketModel;

    public SocketModel getSocketModel() {
        return socketModel;
    }

    public void setSocketModel(SocketModel socketModel) {
        this.socketModel = socketModel;
    }

    public LoginModel() {
    }

    public Document sendLoginRequest(String email, String password) {
        
        try {
            // send the login request to the server
            socketModel.getWriter().write("<login_request><email>"+email+"</email><password>"+password+"</password></login_request>\nEND\n");
            socketModel.getWriter().flush();

            StringBuilder response = new StringBuilder();
            String line;

            // read the response of the server
            while((line = socketModel.getReader().readLine()) != null && !line.equals("END")) {
                response.append(line);
            }
            
            // convert the String response to a Document object
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(response.toString()));
            
            return builder.parse(inputSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
