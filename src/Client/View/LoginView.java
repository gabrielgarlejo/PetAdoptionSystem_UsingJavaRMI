package Client.View;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LoginView extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginView() {
        setTitle("Pet Adoption System - Login");
        setSize(600, 250);  // Adjusted width to accommodate logo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set the layout to BorderLayout for better positioning
        setLayout(new BorderLayout());

        // Panel for the right side login form
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout()); // GridBagLayout for better control over form components
        GridBagConstraints constraints = new GridBagConstraints();

        // Email label and field
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 5, 10);
        panel.add(new JLabel("Email:"), constraints);

        emailField = new JTextField(10); // Set a preferred size
        constraints.gridx = 1;
        panel.add(emailField, constraints);

        // Password label and field
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(new JLabel("Password:"), constraints);

        passwordField = new JPasswordField(10); // Set a preferred size
        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        // Panel for login and register buttons next to each other
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // FlowLayout to put buttons side by side
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2; // Span across both columns
        panel.add(buttonPanel, constraints);

        // Create a right panel with smaller width to move it closer to the logo
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(320, 250));  // Adjusted width to bring the form closer to the logo
        rightPanel.add(panel, BorderLayout.CENTER);

        // Add the right panel to the main frame
        add(rightPanel, BorderLayout.CENTER);

        // Add the logo to the left panel
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 250));  // Adjust the size as needed

        // Load the logo image (adjust the path as necessary)
        ImageIcon logoIcon = new ImageIcon("src/res/petadopt.png"); // Replace with your actual logo file path
        JLabel logoLabel = new JLabel(logoIcon);

        // Add the logo to the left panel
        leftPanel.add(logoLabel);

        // Add the left panel with the logo to the frame
        add(leftPanel, BorderLayout.WEST);
    }

    public String getEmail() {
        return emailField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}



