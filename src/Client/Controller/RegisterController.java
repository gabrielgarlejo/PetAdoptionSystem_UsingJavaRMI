package Client.Controller;

import Client.Model.LoginModel;
import Client.Model.RegisterModel;
import Client.View.LoginView;
import Client.View.RegisterView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import org.w3c.dom.Document;

public class RegisterController {
    private RegisterView view;
    private RegisterModel model;

    public RegisterController(RegisterView view, RegisterModel model) {
        this.view = view;
        this.model = model;

        // Register the listeners for both buttons
        this.view.addRegisterListener(new RegisterListener());
        this.view.addBackListener(new BackListener());
    }

    class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String firstName = view.getFirstName();
            String lastName = view.getLastName();
            String email = view.getEmail();
            String password = view.getPassword();
            String contactNumber = view.getContactNumber();

            // Validate input fields
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || contactNumber.isEmpty()) {
                view.showMessage("All fields must be filled.");
                return;
            }

            // Validate email format using regex
            if (!isValidEmail(email)) {
                view.showMessage("Invalid email format.");
                return;
            }

            // Validate contact number (should only contain digits)
            if (!contactNumber.matches("\\d+")) {
                view.showMessage("Contact number must contain only digits.");
                return;
            }

            // Call the model to register the user
            Document responseDocument = model.registerUser(firstName, lastName, email, password, contactNumber);

            if (responseDocument.getElementsByTagName("status").item(0).getTextContent().equalsIgnoreCase("true")) {
                view.showMessage(responseDocument.getElementsByTagName("message").item(0).getTextContent());
                view.dispose(); // Close the registration window
                // Optionally, open the LoginView after successful registration
                LoginView loginView = new LoginView();
                LoginModel loginModel = new LoginModel();
                new LoginController(loginView, loginModel);

                loginView.setVisible(true);
            } else {
                view.showMessage(responseDocument.getElementsByTagName("message").item(0).getTextContent());
            }
        }
    }

    // Helper method to validate email format using regex
    private boolean isValidEmail(String email) {
        // Simple email validation regex pattern
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    class BackListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose();  // Close the registration window
            
            LoginView loginView = new LoginView();
            LoginModel loginModel = new LoginModel();
            new LoginController(loginView, loginModel);

            RegisterView registerView = new RegisterView();
            RegisterModel registerModel = new RegisterModel();
            new RegisterController(registerView, registerModel);

            loginView.setVisible(true);
        }
    }
}