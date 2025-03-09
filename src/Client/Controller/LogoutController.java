package Client.Controller;


import Client.View.LoginView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

public class LogoutController {
    private JFrame currentView;

    public LogoutController(JFrame currentView) {
        this.currentView = currentView;
        addLogoutListener();
    }

    private void addLogoutListener() {
        if (currentView instanceof Client.View.DashboardView) {
            ((Client.View.DashboardView) currentView).addLogoutListener(new LogoutListener());
        } else if (currentView instanceof Client.View.ManagerDBView) {
            ((Client.View.ManagerDBView) currentView).addLogoutListener(new LogoutListener());
        }
    }

    class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentView.dispose(); // Close current dashboard
            LoginView loginView = new LoginView();
            new Client.Controller.LoginController(loginView, new Client.Model.LoginModel());
            loginView.setVisible(true);
        }
    }
}
