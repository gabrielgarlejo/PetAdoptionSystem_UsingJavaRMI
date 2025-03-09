package Client.View;

import Client.Controller.PetDetailsDialogController;
import static Client.Model.DashboardModel.IMAGE_HEIGHT;
import static Client.Model.DashboardModel.IMAGE_WIDTH;
import Client.Model.SocketModel;
import Common.Pet;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;

public class DashboardView extends JFrame {

    // Main components
    private JTabbedPane tabbedPane;
    private JPanel petsPanel, historyPanel, accountPanel;

    // Pets Panel Components
    private JTextField searchField;
    private JTextField firstNameField, lastNameField, contactNumberField;
    private JButton searchButton, dogsButton, catsButton, reptilesButton, birdsButton;
    private JPanel imagePanel;

    // History Panel Components
    private JTextArea historyTextArea;

    // Account Panel Components
    private JTextField emailField, nameField;
    private JButton updateAccountButton;
    private JButton logoutButton;
    private String currentCategory = null;

    private SocketModel socketModel; // so it can be passed to the PetDetailsDialogController
    private String userUUID;

    public DashboardView(SocketModel socketModel, String userUUID) {
        this.socketModel = socketModel;
        this.userUUID = userUUID;

        setTitle("Pet Adoption System - Adopter Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Create panels
        createPetsPanel();
        createHistoryPanel();
        createAccountPanel();

        // Add tabs
        tabbedPane.addTab("Available Pets", new ImageIcon("icons/pets.png"), petsPanel);
        tabbedPane.addTab("Adoption History", new ImageIcon("icons/history.png"), historyPanel);
        tabbedPane.addTab("Account Info", new ImageIcon("icons/account.png"), accountPanel);

        add(tabbedPane);
    }

    public void setAccountDetails(String email, String firstName, String lastName, String contactNumber) {
        emailField.setText(email);
        firstNameField.setText(firstName);
        lastNameField.setText(lastName);
        contactNumberField.setText(contactNumber);

        emailField.setEditable(false);
    }

    private void createPetsPanel() {
        petsPanel = new JPanel(new GridBagLayout());
        petsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchField = new JTextField(20);
        searchButton = createStyledButton("Search");
        searchPanel.add(new JLabel("Search Pets: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add Search Panel to GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        petsPanel.add(searchPanel, gbc);

        // Category Buttons
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dogsButton = createStyledButton("Dogs");
        catsButton = createStyledButton("Cats");
        reptilesButton = createStyledButton("Reptiles");
        birdsButton = createStyledButton("Birds");
        categoryPanel.add(dogsButton);
        categoryPanel.add(catsButton);
        categoryPanel.add(reptilesButton);
        categoryPanel.add(birdsButton);

        // Add Category Panel to GridBagLayout
        gbc.gridy = 1;
        petsPanel.add(categoryPanel, gbc);

        // Image Panel
        imagePanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Pets"));
        scrollPane.setPreferredSize(new Dimension(700, 300));

        // Add Image Panel to GridBagLayout
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        petsPanel.add(scrollPane, gbc);
    }

    private void createHistoryPanel() {
        historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // Initialize the JTextArea
        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        historyTextArea.setLineWrap(true);
        historyTextArea.setWrapStyleWord(true);
        historyTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
    
        // Add to JScrollPane
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Adoption History"));
    
        historyPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createAccountPanel() {
        accountPanel = new JPanel(new GridBagLayout());
        accountPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Email Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        accountPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountPanel.add(emailField, gbc);

        // First Name Field
        gbc.gridx = 0;
        gbc.gridy++;
        accountPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        accountPanel.add(firstNameField, gbc);

        // Last Name Field
        gbc.gridx = 0;
        gbc.gridy++;
        accountPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        accountPanel.add(lastNameField, gbc);

        // Contact Number Field
        gbc.gridx = 0;
        gbc.gridy++;
        accountPanel.add(new JLabel("Contact Number:"), gbc);
        gbc.gridx = 1;
        contactNumberField = new JTextField(20);
        accountPanel.add(contactNumberField, gbc);

        // Update Account Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2; // Span across two columns
        updateAccountButton = createStyledButton("Update Account Info");
        accountPanel.add(updateAccountButton, gbc);

        gbc.gridy++;
        logoutButton = createStyledButton("Logout");
        accountPanel.add(logoutButton, gbc);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        return button;
    }

    private ImageIcon resizeImage(ImageIcon icon) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    public void addTabbedPaneChangeListener(ChangeListener listener) {
        // ChangeListener to detect tab changes 
        tabbedPane.addChangeListener(listener);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void updateAdoptionsHistoryText(String historyData) {
        historyTextArea.setText(historyData);
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void addSearchListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void addDogsListener(ActionListener listener) {
        dogsButton.addActionListener(e -> {
            currentCategory = "dogs";
            listener.actionPerformed(e);
        });
    }

    public void addCatsListener(ActionListener listener) {
        catsButton.addActionListener(e -> {
            currentCategory = "cats";
            listener.actionPerformed(e);
        });
    }

    public void addReptilesListener(ActionListener listener) {
        reptilesButton.addActionListener(e -> {
            currentCategory = "reptiles";
            listener.actionPerformed(e);
        });
    }

    public void addBirdsListener(ActionListener listener) {
        birdsButton.addActionListener(e -> {
            currentCategory = "birds";
            listener.actionPerformed(e);
        });
    }

    public void displayAdoptionsHistory(Document adoptionsHistoryDocument) {

    }

    public void displayImages(List<Pet> pets) {
        imagePanel.removeAll();
        for (Pet pet : pets) {
            // Only show available pets
            if (pet.getStatus().equalsIgnoreCase("Available")) {
                ImageIcon icon = base64ToImageIcon(pet.getImageBase64Encoded());
                Image img = icon.getImage();
                Image resizedImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(resizedImg);

                JLabel label = new JLabel(resizedIcon);
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                label.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));

                label.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        PetDetailsDialog dialog = new PetDetailsDialog(DashboardView.this, pet, resizedIcon);
                        new PetDetailsDialogController(dialog, pet, userUUID, socketModel);  // Create controller
                        dialog.setVisible(true);

                        // Refresh the dashboard after dialog closes
                        if (pet.getStatus().equalsIgnoreCase("Unavailable")) {
                            refreshView();
                        }
                    }
                });

                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imagePanel.add(label);
            }
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    public void refreshView() {
        // Trigger the current filter/search again
        if (currentCategory != null) {
            switch (currentCategory.toLowerCase()) {
                case "dogs":
                    dogsButton.doClick();
                    break;
                case "cats":
                    catsButton.doClick();
                    break;
                case "birds":
                    birdsButton.doClick();
                    break;
                case "reptiles":
                    reptilesButton.doClick();
                    break;
                default:
                    // If no category is selected, refresh search results
                    searchButton.doClick();
                    break;
            }
        }
    }

    public void displayPets(List<Pet> pets) {
        imagePanel.removeAll(); // Clear previous images
        for (Pet pet : pets) {
            ImageIcon icon = base64ToImageIcon(pet.getImageBase64Encoded());
            JLabel label = new JLabel(icon);
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));

            // Add click listener to the label
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    PetDetailsDialog dialog = new PetDetailsDialog(DashboardView.this, pet, icon);
                    dialog.setVisible(true);
                }
            });

            // Make the label look clickable
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            imagePanel.add(label);
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    public String getEmail() {
        return emailField.getText();
    }

    public String getName() {
        return nameField.getText();
    }

    public void addUpdateAccountListener(ActionListener listener) {
        updateAccountButton.addActionListener(listener);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public String getFirstName() {
        return firstNameField.getText();
    }

    public String getLastName() {
        return lastNameField.getText();
    }

    public String getContactNumber() {
        return contactNumberField.getText();
    }
    
    static ImageIcon base64ToImageIcon(String base64String) {
        try {
            // Decode Base64 string to byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            
            // Convert byte array to BufferedImage
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
            
            // Create ImageIcon from the BufferedImage
            return new ImageIcon(bufferedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null in case of an error
        }
    }
}
