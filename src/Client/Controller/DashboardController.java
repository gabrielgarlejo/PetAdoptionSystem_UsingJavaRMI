package Client.Controller;

import Client.Model.DashboardModel;
import Client.Model.LoginModel;
import Client.View.DashboardView;
import Client.View.LoginView;
import Common.AdopterAccount;
import Common.Pet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DashboardController {
    private DashboardView view;
    private DashboardModel model;
    private String userUUID; // uuid of the logged in user

    public DashboardController(DashboardView view, DashboardModel model, String userUUID) {
        this.view = view;
        this.model = model;
        this.userUUID = userUUID;

        this.model.retrievePetsList();

        // Add action listeners
        this.view.addSearchListener(new SearchListener());
        this.view.addDogsListener(new DogsListener());
        this.view.addCatsListener(new CatsListener());
        this.view.addReptilesListener(new ReptilesListener());
        this.view.addBirdsListener(new BirdsListener());
        this.view.addLogoutListener(new LogoutListener());
        this.view.addUpdateAccountListener(new UpdateAccountListener());
        this.view.addTabbedPaneChangeListener(new TabbedPaneChangeListener());
    }
    
    class TabbedPaneChangeListener implements ChangeListener {
        @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = view.getTabbedPane().getSelectedIndex();
                String tabName = view.getTabbedPane().getTitleAt(selectedIndex);

                if(tabName.equalsIgnoreCase("Adoption History")) {
                    loadUserAdoptionsHistory();
                }

                if(tabName.equalsIgnoreCase("Account Info")) {
                    loadUserDetails();
                }
            }
    }

    // Listener for the search button
    class SearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchText = view.getSearchText();
            List<Pet> results = model.searchPets(searchText);
            view.displayImages(results);
        }
    }

    class UpdateAccountListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = view.getEmail();
            String firstName = view.getFirstName();
            String lastName = view.getLastName();
            String contactNumber = view.getContactNumber();

            // Validate inputs
            if (firstName.trim().isEmpty() || lastName.trim().isEmpty() || contactNumber.trim().isEmpty()) {
                view.showMessage("Please fill in all fields!");
                return;
            }

            try {
                String message = model.updateAccountDetails(userUUID, email, firstName, lastName, contactNumber);
                view.showMessage(message);
            } catch (Exception ex) {
                view.showMessage("Error updating account details. Please try again.");
                ex.printStackTrace();
            }
        }
    }

    class DogsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) { 
            model.retrievePetsList(); // re-read the pets list from the server 
            List<Pet> pets = model.getPets();
            List<Pet> dogPets = new ArrayList<>();
            for (Pet pet : pets) {
                if (pet.getCategory().equalsIgnoreCase("Dogs")) {
                    dogPets.add(pet);
                }
            }
            view.displayImages(dogPets);
        }
    }

    class CatsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.retrievePetsList(); // re-read the pets list from the server 
            List<Pet> pets = model.getPets();
            List<Pet> catPets = new ArrayList<>();
            for (Pet pet : pets) {
                if (pet.getCategory().equalsIgnoreCase("Cats")) {
                    catPets.add(pet);
                }
            }
            view.displayImages(catPets);
        }
    }

    class BirdsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.retrievePetsList(); // re-read the pets list from the server 
            List<Pet> pets = model.getPets();
            List<Pet> birdPets = new ArrayList<>();
            for (Pet pet : pets) {
                if (pet.getCategory().equalsIgnoreCase("Birds")) {
                    birdPets.add(pet);
                }
            }
            view.displayImages(birdPets);
        }
    }

    class ReptilesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.retrievePetsList(); // re-read the pets list from the server 
            List<Pet> pets = model.getPets();
            List<Pet> reptilePets = new ArrayList<>();
            for (Pet pet : pets) {
                if (pet.getCategory().equalsIgnoreCase("Reptiles")) {
                    reptilePets.add(pet);
                }
            }
            view.displayImages(reptilePets);
        }
    }

    class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                model.logout(); // closes the socket and streams of SocketModel
                view.showMessage("You have logged out");
            } catch (IOException ex) {
                view.showMessage("Logout Error: Something went wrong!");
                ex.printStackTrace();
            }

            view.dispose(); // Close adopter's dashboard

            LoginView loginView = new LoginView();
            LoginModel loginModel = new LoginModel();
            new LoginController(loginView, loginModel); // Create new controller
            loginView.setVisible(true);
        }
    }

    private void loadUserDetails() {
        AdopterAccount account = this.model.retrieveAdopterDetails(this.userUUID);
        if (account != null) {
            view.setAccountDetails(
                    account.getEmail(),
                    account.getFirstName(),
                    account.getLastName(),
                    account.getContactNumber()
            );
        }
    }

    private void loadUserAdoptionsHistory() {
        view.updateAdoptionsHistoryText(this.model.retrieveAdoptionsHistory());
    }
}
