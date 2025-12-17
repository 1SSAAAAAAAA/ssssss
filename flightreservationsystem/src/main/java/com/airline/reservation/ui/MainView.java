package com.airline.reservation.ui;

import com.airline.reservation.service.AuthenticationService;
import com.airline.reservation.ui.admin.FlightManagementView;
import com.airline.reservation.ui.admin.PassengerListView;
import com.airline.reservation.ui.admin.UserManagementView;
import com.airline.reservation.ui.components.SidebarView;
import com.airline.reservation.ui.components.StyleConstants;
import com.airline.reservation.ui.passenger.BookingCreationView;
import com.airline.reservation.ui.passenger.FlightSearchView;
import com.airline.reservation.ui.passenger.MyBookingsView;
import com.airline.reservation.ui.staff.BookingView;
import com.airline.reservation.ui.staff.FlightViewView;
import com.airline.reservation.ui.staff.PassengerManagementView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.Optional;

public class MainView extends BorderPane {
    private final AuthenticationService authService;
    private final SidebarView sidebar;

    public MainView(AuthenticationService authService) {
        this.authService = authService;
        
        sidebar = new SidebarView(authService.getCurrentUser().getRole());
        setLeft(sidebar);
        
        setStyle("-fx-background-color: " + StyleConstants.BACKGROUND_COLOR + ";");
        
        setupMenuListeners();
        showDefaultView();
    }

    private void setupMenuListeners() {
        switch (authService.getCurrentUser().getRole()) {
            case ADMINISTRATOR:
                sidebar.addMenuListener("flights", this::showFlightManagement);
                sidebar.addMenuListener("users", this::showUserManagement);
                sidebar.addMenuListener("bookings", this::showAllBookings);
                sidebar.addMenuListener("passengers", this::showPassengerList);
                break;
                
            case STAFF:
                sidebar.addMenuListener("flights", this::showFlightView);
                sidebar.addMenuListener("booking", this::showBookingView);
                sidebar.addMenuListener("passengers", this::showPassengerManagement);
                break;
                
            case PASSENGER:
                sidebar.addMenuListener("search", this::showFlightSearch);
                sidebar.addMenuListener("mybookings", this::showMyBookings);
                sidebar.addMenuListener("book", this::showBookingCreation);
                break;
        }
        
        sidebar.addMenuListener("logout", this::performLogout);
    }

    private void showDefaultView() {
        switch (authService.getCurrentUser().getRole()) {
            case ADMINISTRATOR:
                showFlightManagement();
                break;
            case STAFF:
                showFlightView();
                break;
            case PASSENGER:
                showFlightSearch();
                break;
        }
    }

    private void showFlightManagement() {
        setCenter(new FlightManagementView());
    }

    private void showUserManagement() {
        setCenter(new UserManagementView());
    }

    private void showAllBookings() {
        setCenter(new BookingView(authService));
    }

    private void showPassengerList() {
        setCenter(new PassengerListView());
    }

    private void showFlightView() {
        setCenter(new FlightViewView());
    }

    private void showBookingView() {
        setCenter(new BookingView(authService));
    }

    private void showPassengerManagement() {
        setCenter(new PassengerManagementView());
    }

    private void showFlightSearch() {
        setCenter(new FlightSearchView());
    }

    private void showMyBookings() {
        setCenter(new MyBookingsView(authService));
    }

    private void showBookingCreation() {
        setCenter(new BookingCreationView(authService));
    }

    private void performLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            authService.logout();
            Stage stage = (Stage) getScene().getWindow();
            LoginView loginView = new LoginView(stage);
            stage.getScene().setRoot(loginView);
            stage.setWidth(480);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.setResizable(false);
        }
    }
}