package com.airline.reservation.ui.admin;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.airline.reservation.model.Passenger;
import com.airline.reservation.service.PassengerService;
import com.airline.reservation.ui.components.StyleConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PassengerListPanel extends VBox {
    private final PassengerService passengerService;
    private TableView<Passenger> passengerTable;
    private ObservableList<Passenger> passengerData;
    private TextField searchField;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PassengerListPanel() {
        this.passengerService = new PassengerService();
        this.passengerData = FXCollections.observableArrayList();
        initializeComponents();
        loadPassengers();
    }

    private void initializeComponents() {
        setSpacing(StyleConstants.SPACING_MD);
        setPadding(StyleConstants.PADDING_MD);
        setStyle("-fx-background-color: " + StyleConstants.BACKGROUND_COLOR + ";");

        // Header
        HBox header = createHeader();
        
        // Content card
        Card contentCard = new Card();
        contentCard.setBody(createTableView());
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        getChildren().addAll(header, contentCard);
    }

    private HBox createHeader() {
        Label titleLabel = new Label("Passenger List");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.TEXT_PRIMARY + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search passengers...");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(StyleConstants.INPUT_HEIGHT);
        searchField.textProperty().addListener((obs, old, newVal) -> filterPassengers(newVal));

        // Refresh button
        Button refreshButton = new Button("", new FontIcon(MaterialDesignR.REFRESH));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        refreshButton.setPrefHeight(StyleConstants.BUTTON_HEIGHT);
        refreshButton.setOnAction(e -> loadPassengers());

        HBox buttonBox = new HBox(StyleConstants.SPACING_SM, searchField, refreshButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(titleLabel, spacer, buttonBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, StyleConstants.SPACING_MD, 0));

        return header;
    }

    private VBox createTableView() {
        passengerTable = new TableView<>();
        passengerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(passengerTable, Priority.ALWAYS);

        // Columns
        TableColumn<Passenger, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("passengerId"));
        idCol.setPrefWidth(60);

        TableColumn<Passenger, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Passenger, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Passenger, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dobCol.setCellFactory(col -> new TableCell<Passenger, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        TableColumn<Passenger, String> passportCol = new TableColumn<>("Passport Number");
        passportCol.setCellValueFactory(new PropertyValueFactory<>("passportNumber"));

        TableColumn<Passenger, String> nationalityCol = new TableColumn<>("Nationality");
        nationalityCol.setCellValueFactory(new PropertyValueFactory<>("nationality"));

        TableColumn<Passenger, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Passenger, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Passenger, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(col -> new TableCell<Passenger, Void>() {
            private final Button viewBtn = new Button("", new FontIcon(MaterialDesignM.MAGNIFY));
            private final HBox pane = new HBox(8, viewBtn);

            {
                viewBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
                pane.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(e -> {
                    Passenger passenger = getTableView().getItems().get(getIndex());
                    showPassengerDetails(passenger);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        passengerTable.getColumns().addAll(idCol, firstNameCol, lastNameCol, dobCol, 
                                           passportCol, nationalityCol, emailCol, phoneCol, actionsCol);
        passengerTable.setItems(passengerData);

        return new VBox(passengerTable);
    }

    private void loadPassengers() {
        try {
            List<Passenger> passengers = passengerService.getAllPassengers();
            passengerData.setAll(passengers);
        } catch (SQLException e) {
            showError("Failed to load passengers: " + e.getMessage());
        }
    }

    private void filterPassengers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadPassengers();
            return;
        }
        
        try {
            List<Passenger> allPassengers = passengerService.getAllPassengers();
            List<Passenger> filtered = allPassengers.stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(searchText.toLowerCase()) ||
                           p.getLastName().toLowerCase().contains(searchText.toLowerCase()) ||
                           (p.getPassportNumber() != null && p.getPassportNumber().toLowerCase().contains(searchText.toLowerCase())) ||
                           (p.getEmail() != null && p.getEmail().toLowerCase().contains(searchText.toLowerCase())))
                .toList();
            passengerData.setAll(filtered);
        } catch (SQLException e) {
            showError("Failed to filter passengers: " + e.getMessage());
        }
    }

    private void showPassengerDetails(Passenger passenger) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Passenger Details");
        alert.setHeaderText(passenger.getFirstName() + " " + passenger.getLastName());

        GridPane grid = new GridPane();
        grid.setHgap(StyleConstants.SPACING_SM);
        grid.setVgap(StyleConstants.SPACING_SM);
        grid.setPadding(StyleConstants.PADDING_MD);

        grid.add(new Label("Passenger ID:"), 0, 0);
        grid.add(new Label(String.valueOf(passenger.getPassengerId())), 1, 0);
        
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(new Label(passenger.getFirstName() + " " + passenger.getLastName()), 1, 1);
        
        grid.add(new Label("Date of Birth:"), 0, 2);
        grid.add(new Label(passenger.getDateOfBirth().format(formatter)), 1, 2);
        
        grid.add(new Label("Passport:"), 0, 3);
        grid.add(new Label(passenger.getPassportNumber()), 1, 3);
        
        grid.add(new Label("Nationality:"), 0, 4);
        grid.add(new Label(passenger.getNationality()), 1, 4);
        
        grid.add(new Label("Email:"), 0, 5);
        grid.add(new Label(passenger.getEmail()), 1, 5);
        
        grid.add(new Label("Phone:"), 0, 6);
        grid.add(new Label(passenger.getPhoneNumber()), 1, 6);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}