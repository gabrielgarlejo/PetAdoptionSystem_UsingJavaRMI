package Client;

import Client.Controller.LoginController;
import Client.Controller.RegisterController;
import Client.Model.LoginModel;
import Client.Model.RegisterModel;
import Client.View.LoginView;
import Client.View.RegisterView;

public class PetAdoptionSystem {
    public static void main(String[] args) {
        LoginView loginView = new LoginView();
        LoginModel loginModel = new LoginModel();
        new LoginController(loginView, loginModel);

        RegisterView registerView = new RegisterView();
        RegisterModel registerModel = new RegisterModel();
        new RegisterController(registerView, registerModel);

        loginView.setVisible(true);
    }
}
