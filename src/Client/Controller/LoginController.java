package Client.Controller;

import Client.Model.DashboardModel;
import Client.Model.LoginModel;
import Client.Model.ManagerDBModel;
import Client.Model.RegisterModel;
import Client.Model.SocketModel;
import Client.View.DashboardView;
import Client.View.LoginView;
import Client.View.ManagerDBView;
import Client.View.RegisterView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import org.w3c.dom.Document;

public class LoginController {
    private LoginView view;
    private LoginModel model;
    private SocketModel socketModel;
    private final String HOST_ADDRESS = "localhost";
    private final int PORT_NUMBER = 5000;

    public LoginController(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;
        this.view.addRegisterListener(new RegisterListener());
        this.view.addLoginListener(new LoginListener());
    }

    // Opens the registration form
    class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose();
            RegisterView registerView = new RegisterView();
            RegisterModel registerModel = new RegisterModel();
            new RegisterController(registerView, registerModel);
            registerView.setVisible(true);
        }
    }

    // Handles login action and opens the correct dashboard based on user type
    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = view.getEmail();
            String password = view.getPassword();

            if (email.isEmpty() || password.isEmpty()) {
                view.showMessage("Please fill in both email and password.");
                return;
            }

            try {
                socketModel = new SocketModel(new Socket(HOST_ADDRESS, PORT_NUMBER));                
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            model.setSocketModel(socketModel);
            Document response = model.sendLoginRequest(email, password);
            response.normalize();

            String userUUID = response.getElementsByTagName("uuid").item(0).getTextContent();
            String userRole = response.getElementsByTagName("role").item(0).getTextContent();

            if (!userRole.equalsIgnoreCase("null")) {
                view.showMessage("Login Successful!");
                view.dispose();
                if (userRole.equals("adopter")) {

                    DashboardView dashboardView = new DashboardView(socketModel, userUUID);
                    DashboardModel dashboardModel = new DashboardModel(socketModel, userUUID);

                    new DashboardController(dashboardView, dashboardModel, userUUID);
                    dashboardView.setVisible(true);
                } else if (userRole.equals("manager")) {

                    ManagerDBView managerView = new ManagerDBView();
                    ManagerDBModel managerModel = new ManagerDBModel(socketModel);
                    new ManagerDBController(managerView, managerModel, userUUID);
                    managerView.setVisible(true);
                }
            } else {
                view.showMessage(response.getElementsByTagName("message").item(0).getTextContent());
            }
        }
    }
}