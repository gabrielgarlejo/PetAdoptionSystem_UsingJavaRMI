package Client.View;

import Common.Pet;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PetDetailsDialog extends JDialog {
    private JLabel imageLabel;
    private JLabel idLabel;
    private JLabel nameLabel;
    private JLabel breedLabel;
    private JLabel ageLabel;
    private JLabel categoryLabel;
    private JLabel statusLabel;
    private JButton backButton;
    private JButton adoptButton;

    public PetDetailsDialog(JFrame parent, Pet pet, ImageIcon petImage) {
        super(parent, "Pet Details", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left panel for image
        JPanel imagePanel = new JPanel();
        imageLabel = new JLabel(petImage);
        imagePanel.add(imageLabel);
        mainPanel.add(imagePanel, BorderLayout.WEST);

        // Right panel for details
        JPanel detailsPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        idLabel = new JLabel("ID: " + pet.getId());
        nameLabel = new JLabel("Name: " + pet.getName());
        breedLabel = new JLabel("Breed: " + pet.getBreed());
        ageLabel = new JLabel("Age: " + pet.getAge());
        categoryLabel = new JLabel("Category: " + pet.getCategory());
        statusLabel = new JLabel("Status: " + pet.getStatus());

        detailsPanel.add(idLabel);
        detailsPanel.add(nameLabel);
        detailsPanel.add(breedLabel);
        detailsPanel.add(ageLabel);
        detailsPanel.add(categoryLabel);
        detailsPanel.add(statusLabel);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        // Back button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        adoptButton = new JButton("Adopt");
        buttonPanel.add(adoptButton);

        add(mainPanel);
    }

    public void addAdoptListener(ActionListener listener) {
        adoptButton.addActionListener(listener);
    }

    public void updateStatus(String status) {
        statusLabel.setText("Status: " + status);
    }

    public void disableAdoptButton() {

        adoptButton.setEnabled(false);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}