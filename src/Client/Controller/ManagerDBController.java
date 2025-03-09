package Client.Controller;

import Client.Controller.ManagerDBController.AddPetListener;
import Client.Controller.ManagerDBController.EditPetListener;
import Client.Controller.ManagerDBController.UploadImageListener;
import Client.Model.LoginModel;
import Client.Model.ManagerDBModel;
import Client.View.LoginView;
import Client.View.ManagerDBView;
import Common.Pet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;

public class ManagerDBController {
    private ManagerDBView view;
    private ManagerDBModel model;
    private String currentImagePath;
    private String userUUID;

    public ManagerDBController(ManagerDBView view, ManagerDBModel model, String userUUID) {
        this.view = view;
        this.model = model;
        this.userUUID = userUUID;

        // Set the controller reference in the view
        this.view.setController(this);

        // Add listeners
        this.view.addPetListener(new AddPetListener());
        this.view.addEditPetListener(new EditPetListener());
        this.view.addSearchListener(new SearchListener());
        this.view.addUploadImageListener(new UploadImageListener());
        this.view.addLogoutListener(new LogoutListener());
        this.view.addDeletePetListener(new DeletePetListener());
        this.view.addFilterButtonListener(new FilterButtonActionListener());
        this.view.addTabbedPaneChangeListener(new TabbedPaneChangeListener());

        // Display pets when the view is initialized
        displayPets();
    }

    class TabbedPaneChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int selectedIndex = view.getTabbedPane().getSelectedIndex();
            String tabName = view.getTabbedPane().getTitleAt(selectedIndex);
            System.out.println("Selected Tab: " + tabName);
            
            if(tabName.equalsIgnoreCase("Pets Management")) {
                view.populatePetsTable(model.getPets()); // refreshes the table
            } else if (tabName.equalsIgnoreCase("Adoptions")) {
                // if needed
            } else if (tabName.equalsIgnoreCase("Reports")) {
                view.populateReports(model.getServerLogs());
            }
        }
    }

    class FilterButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Placeholder for filter logic (can call filteredAdoptionsHistory() here)
            String startDate = view.getStartDateField().getText();
            String endDate = view.getEndDateField().getText();
            
            view.getadoptionsHistoryArea().setText("Filtering adoptions between " + startDate + " and " + endDate + "...\n" + (model.getFilteredAdoptionsHistory(startDate, endDate)));
        }
    }

    public void displayPets() {
        List<Pet> pets = model.getPets();
        view.populatePetsTable(pets);
    }

    class AddPetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String name = view.getPetName();
                String breed = view.getBreed();
                int age = view.getAge();
                String category = view.getCategory();
                String description = view.getDescription();

                if (name.isEmpty() || breed.isEmpty() || description.isEmpty() || currentImagePath == null) {
                    view.showMessage("Please fill in all fields and upload an image");
                    return;
                }

                Document responseDocument = model.addPet(name, breed, age, category, description, currentImagePath);
                boolean success = responseDocument.getElementsByTagName("status").item(0).getTextContent().equalsIgnoreCase("true");
                String message = responseDocument.getElementsByTagName("message").item(0).getTextContent();

                if (success) {
                    view.showMessage(message);
                    displayPets(); // Refresh the pet list after adding a new pet
                } else {
                    view.showMessage(message);
                }
            } catch (NumberFormatException ex) {
                view.showMessage("Please enter a valid age");
            }
        }
    }

    class SearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String searchQuery = view.getSearchQuery();
                List<Pet> searchResults;

                if (searchQuery.isEmpty()) {
                    searchResults = model.getPets();
                } else {
                    searchResults = model.searchPets(searchQuery);
                }

                view.populatePetsTable(searchResults);
            } catch (Exception ex) {
                view.showMessage("Error searching pets: " + ex.getMessage());
            }
        }
    }

    class UploadImageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(view);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                currentImagePath = selectedFile.getAbsolutePath();
            }
        }
    }
    
    class EditPetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = view.getSelectedPetRow();
            if (selectedRow != -1) {
                String id = view.getPetId(selectedRow);
                String name = view.getPetName().trim();
                String breed = view.getBreed().trim();
                int age = view.getAge();
                String category = view.getCategory();
                String description = view.getDescription().trim();

                // Only validate non-empty fields
                if (name.isEmpty() && breed.isEmpty() && description.isEmpty()) {
                    view.showMessage("Please modify at least one field");
                    return;
                }

                Document responseDocument = model.updatePet(id, name, breed, age, category, description);
                boolean success = responseDocument.getElementsByTagName("status").item(0).getTextContent().equalsIgnoreCase("true");
                String message = responseDocument.getElementsByTagName("message").item(0).getTextContent();

                if (success) {
                    view.showMessage(message);
                    view.populatePetsTable(model.getPets());
                    displayPets(); // Refresh the pet list after updating
                } else {
                    view.showMessage(message);
                }
            } else {
                view.showMessage("Please select a pet to edit");
            }
        }
    }
    
    class DeletePetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = view.getSelectedPetRow();
            if (selectedRow != -1) {
                String petID = view.getPetId(selectedRow);
                String name = view.getPetName().trim();
                String breed = view.getBreed().trim();
                int age = view.getAge();
                String category = view.getCategory();
                String description = view.getDescription().trim();

                // Only validate non-empty fields
                if (name.isEmpty() && breed.isEmpty() && description.isEmpty()) {
                    view.showMessage("Please modify at least one field");
                    return;
                }

                Document responseDocument = model.deletePet(petID);
                boolean success = responseDocument.getElementsByTagName("status").item(0).getTextContent().equalsIgnoreCase("true");
                String message = responseDocument.getElementsByTagName("message").item(0).getTextContent();

                if (success) {
                    view.showMessage(message);
                    view.populatePetsTable(model.getPets());
                    displayPets(); // Refresh the pet list after updating
                } else {
                    view.showMessage(message);
                }
            } else {
                view.showMessage("Please select a pet to edit");
            }
        }
    }

    class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.logout();
            view.dispose(); // Close manager's dashboard
            LoginView loginView = new LoginView();
            LoginModel loginModel = new LoginModel();
            new LoginController(loginView, loginModel); // Create new controller
            loginView.setVisible(true);
        }
    }
}