package Client.View;

import Client.Controller.ManagerDBController;
import Common.Pet;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

//import com.toedter.calendar.JDateChooser; // Make sure you have the JCalendar library in your classpath

public class ManagerDBView extends JFrame {
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel petsPanel, adoptionsPanel, reportsPanel;
    private JButton logoutButton;

    // Pets Panel Components
    private JTextField petNameField, breedField, ageField, searchPetField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    private JButton addPetButton, editPetButton, deletePetButton,uploadImageButton, searchPetButton;
    private JTable petsTable;
    private JLabel imagePreview;
    private ManagerDBController controller;
    private List<Pet> currentPetsList;

    // Adoptions Panel Components
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextArea adoptionsHistoryArea;
    private JButton filterButton;

    // Reports Panel Components
    private JTextArea reportsTextArea;

    public ManagerDBView() {
        setTitle("Pet Adoption System - Manager Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel to hold everything
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create logout panel
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = createStyledButton("Logout");
        logoutPanel.add(logoutButton);
        mainPanel.add(logoutPanel, BorderLayout.NORTH);

        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Create panels
        createPetsPanel();
        createAdoptionsPanel();
        createReportsPanel();

        // Add tabs
        tabbedPane.addTab("Pets Management", new ImageIcon("icons/pets.png"), petsPanel);
        tabbedPane.addTab("Adoptions", new ImageIcon("icons/adoptions.png"), adoptionsPanel);
        tabbedPane.addTab("Reports", new ImageIcon("icons/reports.png"), reportsPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private void createPetsPanel() {
        petsPanel = new JPanel(new BorderLayout(10, 10));
        petsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left panel for adding pets
        JPanel addPetPanel = new JPanel(new GridBagLayout());
        addPetPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Add New Pet",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Pet Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        addPetPanel.add(new JLabel("Pet Name:"), gbc);
        gbc.gridx = 1;
        petNameField = new JTextField(15);
        petNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addPetPanel.add(petNameField, gbc);

        // Row 1: Breed
        gbc.gridx = 0;
        gbc.gridy++;
        addPetPanel.add(new JLabel("Breed:"), gbc);
        gbc.gridx = 1;
        breedField = new JTextField(15);
        breedField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addPetPanel.add(breedField, gbc);

        // Row 2: Age
        gbc.gridx = 0;
        gbc.gridy++;
        addPetPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(15);
        ageField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addPetPanel.add(ageField, gbc);

        // Row 3: Category
        gbc.gridx = 0;
        gbc.gridy++;
        addPetPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryCombo = new JComboBox<>(new String[]{"Dogs", "Cats", "Birds", "Reptiles"});
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryCombo.setPreferredSize(new Dimension(150, 25));
        addPetPanel.add(categoryCombo, gbc);

        // Row 4: Description
        gbc.gridx = 0;
        gbc.gridy++;
        addPetPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(4, 15);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        addPetPanel.add(descriptionScroll, gbc);

        // Row 5: Upload Image Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        uploadImageButton = createStyledButton("Upload Image");
        addPetPanel.add(uploadImageButton, gbc);

        // Row 6: Image Preview
        gbc.gridy++;
        imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(150, 150));
        imagePreview.setBorder(BorderFactory.createEtchedBorder());
        addPetPanel.add(imagePreview, gbc);

        // Row 7: Add Pet Button
        gbc.gridy++;
        addPetButton = createStyledButton("Add Pet");
        addPetPanel.add(addPetButton, gbc);

        // Row 8: Edit Pet Button
        gbc.gridy++;
        editPetButton = createStyledButton("Edit Pet");
        addPetPanel.add(editPetButton, gbc);

        // Row 9: Delete Pet Button
        gbc.gridy++;
        deletePetButton = createStyledButton("Delete Pet");
        addPetPanel.add(deletePetButton, gbc);

        // Left side: add addPetPanel to Pets Panel
        petsPanel.add(addPetPanel, BorderLayout.WEST);

        // Right panel: Pets Table and Search Panel
        String[] columns = {"ID", "Name", "Breed", "Age", "Category", "Status"};
        // With these lines:
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // This makes the table read-only
            }
        };
        petsTable = new JTable(tableModel);
        stylizeTable(petsTable);
        JScrollPane tableScrollPane = new JScrollPane(petsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Available Pets"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search: "));
        searchPetField = createStyledTextField();
        searchPanel.add(searchPetField);
        searchPetButton = createStyledButton("Search");
        searchPanel.add(searchPetButton);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(tableScrollPane, BorderLayout.CENTER);

        petsPanel.add(rightPanel, BorderLayout.CENTER);


        petsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = petsTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Get values from the selected row
                    String name = (String) petsTable.getValueAt(selectedRow, 1);
                    String breed = (String) petsTable.getValueAt(selectedRow, 2);
                    int age = (int) petsTable.getValueAt(selectedRow, 3);
                    String category = (String) petsTable.getValueAt(selectedRow, 4);

                    // Set values in the form fields
                    petNameField.setText(name);
                    breedField.setText(breed);
                    ageField.setText(String.valueOf(age));
                    categoryCombo.setSelectedItem(category);


                    String id = (String) petsTable.getValueAt(selectedRow, 0);
                    for (Pet pet : getCurrentPetsList()) {
                        if (pet.getId().equals(id)) {
                            descriptionArea.setText(pet.getDescription());
                            break;
                        }
                    }
                }
            }
        });
    }



    public void setController(ManagerDBController controller) {
        this.controller = controller;
    }

    private void createAdoptionsPanel() {
        // Initialize the JPanel
        adoptionsPanel = new JPanel();
        adoptionsPanel.setLayout(new BorderLayout());

        // Create the input fields for startDate and endDate
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        // Start Date input
        JLabel startDateLabel = new JLabel("Start Date (yyyy-MM-dd): ");
        startDateField = new JTextField(10);
        inputPanel.add(startDateLabel);
        inputPanel.add(startDateField);

        // End Date input
        JLabel endDateLabel = new JLabel("End Date (yyyy-MM-dd): ");
        endDateField = new JTextField(10);
        inputPanel.add(endDateLabel);
        inputPanel.add(endDateField);

        // Filter button
        filterButton = new JButton("Filter Adoptions");
        inputPanel.add(filterButton);

        // Add inputPanel to the main panel
        adoptionsPanel.add(inputPanel, BorderLayout.CENTER);

        // Create the output area to show filtered adoption history or other information
        adoptionsHistoryArea = new JTextArea(18, 30);
        adoptionsHistoryArea.setFont(new Font("Courier New", Font.PLAIN, 24));
        adoptionsHistoryArea.setMargin(new Insets(10, 10, 10, 10));  // top, left, bottom, right
        adoptionsHistoryArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(adoptionsHistoryArea);
        adoptionsPanel.add(scrollPane, BorderLayout.SOUTH);
    }

    public void addFilterButtonListener(ActionListener listener) {
        filterButton.addActionListener(listener);
    }

    private void createReportsPanel() {        // Initialize the JPanel
        reportsPanel = new JPanel();
        reportsPanel.setLayout(new BorderLayout());  // Use BorderLayout to organize components

        // Create the JTextArea
        reportsTextArea = new JTextArea(22, 30); // Set rows and columns for size
        reportsTextArea.setFont(new Font("Courier New", Font.PLAIN, 18)); // Set custom font and size
        reportsTextArea.setMargin(new Insets(10, 10, 10, 10));  // top, left, bottom, right
        reportsTextArea.setEditable(false); // Make the text area non-editable (if needed)

        // Wrap the JTextArea in a JScrollPane to make it scrollable
        JScrollPane scrollPane = new JScrollPane(reportsTextArea);
        reportsPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void setCurrentPetsList(List<Pet> pets) {
        this.currentPetsList = pets;
    }

    public List<Pet> getCurrentPetsList() {
        return currentPetsList;
    }

    // Helper methods for consistent styling
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return field;
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


    private void stylizeTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(new Color(70, 130, 180, 50));
    }

    // Getter methods for fields (used by the Controller)
    public String getPetName() { return petNameField.getText(); }
    public String getBreed() { return breedField.getText(); }
    public int getAge() {
        String ageText = ageField.getText().trim();
        if (ageText.isEmpty()) {
            return -1; // Return a default value to indicate age is not provided
        }
        try {
            return Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            showMessage("Age must be a number.");
            return -1; // Return a default value to indicate an error
        }
    }

    public String getCategory() { return (String) categoryCombo.getSelectedItem(); }
    public String getDescription() { return descriptionArea.getText(); }

    // Listener registration methods
    public void addPetListener(java.awt.event.ActionListener listener) {
        addPetButton.addActionListener(listener);
    }
    public void addEditPetListener(java.awt.event.ActionListener listener) {
        editPetButton.addActionListener(listener);
    }
    public void addDeletePetListener(java.awt.event.ActionListener listener) {
        deletePetButton.addActionListener(listener);
    }
    public void addSearchListener(java.awt.event.ActionListener listener) {
        searchPetButton.addActionListener(listener);
    }
    public void addUploadImageListener(java.awt.event.ActionListener listener) {
        uploadImageButton.addActionListener(listener);
    }

    // Message dialog method
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    public void populatePetsTable(List<Pet> pets) {
        setCurrentPetsList(pets); // Store the current list
        DefaultTableModel tableModel = (DefaultTableModel) petsTable.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (Pet pet : pets) {
            tableModel.addRow(new Object[]{
                    pet.getId(),
                    pet.getName(),
                    pet.getBreed(),
                    pet.getAge(),
                    pet.getCategory(),
                    pet.getStatus()
            });
        }
    }

    public String getSearchQuery() {
        return searchPetField.getText().trim();
    }

    public int getSelectedPetRow() {
        return petsTable.getSelectedRow(); // Returns the index of the selected row
    }

    public String getPetId(int row) {
        return petsTable.getValueAt(row, 0).toString();
    }
    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    public JTextField getStartDateField() {
        return startDateField;
    }

    public void setStartDateField(JTextField startDateField) {
        this.startDateField = startDateField;
    }

    public JTextField getEndDateField() {
        return endDateField;
    }

    public void setEndDateField(JTextField endDateField) {
        this.endDateField = endDateField;
    }

    public JTextArea getadoptionsHistoryArea() {
        return adoptionsHistoryArea;
    }

    public void setadoptionsHistoryArea(JTextArea adoptionsHistoryArea) {
        this.adoptionsHistoryArea = adoptionsHistoryArea;
    }
    
    public void addTabbedPaneChangeListener(ChangeListener listener) {
        tabbedPane.addChangeListener(listener);
    }
    
    public JTextArea getReportsTextArea() {
        return reportsTextArea;
    }

    public void setReportsTextArea(JTextArea reportsTextArea) {
        this.reportsTextArea = reportsTextArea;
    }

    public void populateReports(String reports) {
        reportsTextArea.setText(reports);
    }
}
