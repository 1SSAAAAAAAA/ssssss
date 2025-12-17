package com.airline.reservation.ui.admin;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.airline.reservation.model.Flight;
import com.airline.reservation.model.enums.FlightStatus;
import com.airline.reservation.service.FlightService;
import com.airline.reservation.ui.components.StyleConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightManagementView extends VBox {
    private final FlightService flightService;
    private TableView<Flight> flightTable;
    private ObservableList<Flight> flightData;
    private TextField searchField;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FlightManagementView() {
        this.flightService = new FlightService();
        this.flightData = FXCollections.observableArrayList();
        initializeComponents();
        loadFlights();
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
        Label titleLabel = new Label("Flight Management");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.TEXT_PRIMARY + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search flights...");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(StyleConstants.INPUT_HEIGHT);
        searchField.textProperty().addListener((obs, old, newVal) -> filterFlights(newVal));

        // Add button
        Button addButton = new Button("Add Flight", new FontIcon(MaterialDesignP.PLUS));
        addButton.getStyleClass().addAll(Styles.ACCENT);
        addButton.setPrefHeight(StyleConstants.BUTTON_HEIGHT);
        addButton.setOnAction(e -> showAddFlightDialog());

        // Refresh button
        Button refreshButton = new Button("", new FontIcon(MaterialDesignR.REFRESH));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        refreshButton.setPrefHeight(StyleConstants.BUTTON_HEIGHT);
        refreshButton.setOnAction(e -> loadFlights());

        HBox buttonBox = new HBox(StyleConstants.SPACING_SM, searchField, addButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(titleLabel, spacer, buttonBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, StyleConstants.SPACING_MD, 0));

        return header;
    }

    private VBox createTableView() {
        flightTable = new TableView<>();
        flightTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(flightTable, Priority.ALWAYS);

        // Columns
        TableColumn<Flight, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        idCol.setPrefWidth(60);

        TableColumn<Flight, String> flightNumCol = new TableColumn<>("Flight Number");
        flightNumCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));

        TableColumn<Flight, String> originCol = new TableColumn<>("Origin");
        originCol.setCellValueFactory(new PropertyValueFactory<>("origin"));

        TableColumn<Flight, String> destCol = new TableColumn<>("Destination");
        destCol.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<Flight, LocalDateTime> deptCol = new TableColumn<>("Departure");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        deptCol.setCellFactory(col -> new TableCell<Flight, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        TableColumn<Flight, LocalDateTime> arrCol = new TableColumn<>("Arrival");
        arrCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        arrCol.setCellFactory(col -> new TableCell<Flight, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        TableColumn<Flight, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> new TableCell<Flight, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        TableColumn<Flight, Integer> seatsCol = new TableColumn<>("Available Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        TableColumn<Flight, FlightStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<Flight, FlightStatus>() {
            @Override
            protected void updateItem(FlightStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    String color = switch (item) {
                        case SCHEDULED -> StyleConstants.PRIMARY_COLOR;
                        case DELAYED -> StyleConstants.WARNING_COLOR;
                        case CANCELLED -> StyleConstants.ERROR_COLOR;
                        case COMPLETED -> StyleConstants.SUCCESS_COLOR;
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Flight, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<Flight, Void>() {
            private final Button editBtn = new Button("", new FontIcon(MaterialDesignP.PENCIL));
            private final Button deleteBtn = new Button("", new FontIcon(MaterialDesignD.DELETE));
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
                deleteBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
                pane.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    showEditFlightDialog(flight);
                });

                deleteBtn.setOnAction(e -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    deleteFlight(flight);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        flightTable.getColumns().addAll(idCol, flightNumCol, originCol, destCol, 
                                        deptCol, arrCol, priceCol, seatsCol, statusCol, actionsCol);
        flightTable.setItems(flightData);

        return new VBox(flightTable);
    }

    private void loadFlights() {
        try {
            List<Flight> flights = flightService.getAllFlights();
            flightData.setAll(flights);
        } catch (SQLException e) {
            showError("Failed to load flights: " + e.getMessage());
        }
    }

    private void filterFlights(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadFlights();
            return;
        }
        
        try {
            List<Flight> allFlights = flightService.getAllFlights();
            List<Flight> filtered = allFlights.stream()
                .filter(f -> f.getFlightNumber().toLowerCase().contains(searchText.toLowerCase()) ||
                           f.getOrigin().toLowerCase().contains(searchText.toLowerCase()) ||
                           f.getDestination().toLowerCase().contains(searchText.toLowerCase()))
                .toList();
            flightData.setAll(filtered);
        } catch (SQLException e) {
            showError("Failed to filter flights: " + e.getMessage());
        }
    }

    private void showAddFlightDialog() {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText("Enter flight details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = createFlightForm(null);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return extractFlightFromForm(grid, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(flight -> {
            try {
                flightService.addFlight(flight);
                loadFlights();
                showSuccess("Flight added successfully!");
            } catch (SQLException e) {
                showError("Failed to add flight: " + e.getMessage());
            }
        });
    }

    private void showEditFlightDialog(Flight flight) {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Edit Flight");
        dialog.setHeaderText("Update flight details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = createFlightForm(flight);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return extractFlightFromForm(grid, flight);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedFlight -> {
            try {
                flightService.updateFlight(updatedFlight);
                loadFlights();
                showSuccess("Flight updated successfully!");
            } catch (SQLException e) {
                showError("Failed to update flight: " + e.getMessage());
            }
        });
    }

    private GridPane createFlightForm(Flight flight) {
        GridPane grid = new GridPane();
        grid.setHgap(StyleConstants.SPACING_SM);
        grid.setVgap(StyleConstants.SPACING_SM);
        grid.setPadding(StyleConstants.PADDING_MD);

        TextField flightNumField = new TextField(flight != null ? flight.getFlightNumber() : "");
        TextField originField = new TextField(flight != null ? flight.getOrigin() : "");
        TextField destField = new TextField(flight != null ? flight.getDestination() : "");
        TextField deptField = new TextField(flight != null ? flight.getDepartureTime().format(formatter) : "");
        TextField arrField = new TextField(flight != null ? flight.getArrivalTime().format(formatter) : "");
        TextField priceField = new TextField(flight != null ? String.valueOf(flight.getPrice()) : "0.0");
        TextField seatsField = new TextField(flight != null ? String.valueOf(flight.getTotalSeats()) : "0");
        ComboBox<FlightStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(FlightStatus.values()));
        statusCombo.setValue(flight != null ? flight.getStatus() : FlightStatus.SCHEDULED);

        flightNumField.setPromptText("e.g., AA123");
        originField.setPromptText("e.g., New York");
        destField.setPromptText("e.g., Los Angeles");
        deptField.setPromptText("yyyy-MM-dd HH:mm");
        arrField.setPromptText("yyyy-MM-dd HH:mm");
        priceField.setPromptText("e.g., 299.99");
        seatsField.setPromptText("e.g., 150");

        grid.add(new Label("Flight Number:"), 0, 0);
        grid.add(flightNumField, 1, 0);
        grid.add(new Label("Origin:"), 0, 1);
        grid.add(originField, 1, 1);
        grid.add(new Label("Destination:"), 0, 2);
        grid.add(destField, 1, 2);
        grid.add(new Label("Departure:"), 0, 3);
        grid.add(deptField, 1, 3);
        grid.add(new Label("Arrival:"), 0, 4);
        grid.add(arrField, 1, 4);
        grid.add(new Label("Price:"), 0, 5);
        grid.add(priceField, 1, 5);
        grid.add(new Label("Total Seats:"), 0, 6);
        grid.add(seatsField, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(statusCombo, 1, 7);

        return grid;
    }

    private Flight extractFlightFromForm(GridPane grid, Flight existingFlight) {
        TextField flightNumField = (TextField) grid.getChildren().get(1);
        TextField originField = (TextField) grid.getChildren().get(3);
        TextField destField = (TextField) grid.getChildren().get(5);
        TextField deptField = (TextField) grid.getChildren().get(7);
        TextField arrField = (TextField) grid.getChildren().get(9);
        TextField priceField = (TextField) grid.getChildren().get(11);
        TextField seatsField = (TextField) grid.getChildren().get(13);
        @SuppressWarnings("unchecked")
        ComboBox<FlightStatus> statusCombo = (ComboBox<FlightStatus>) grid.getChildren().get(15);

        Flight flight = existingFlight != null ? existingFlight : new Flight();
        flight.setFlightNumber(flightNumField.getText());
        flight.setOrigin(originField.getText());
        flight.setDestination(destField.getText());
        flight.setDepartureTime(LocalDateTime.parse(deptField.getText(), formatter));
        flight.setArrivalTime(LocalDateTime.parse(arrField.getText(), formatter));
        flight.setPrice(Double.parseDouble(priceField.getText()));
        flight.setTotalSeats(Integer.parseInt(seatsField.getText()));
        flight.setAvailableSeats(Integer.parseInt(seatsField.getText()));
        flight.setStatus(statusCombo.getValue());

        return flight;
    }

    private void deleteFlight(Flight flight) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Flight");
        confirm.setHeaderText("Delete flight " + flight.getFlightNumber() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    flightService.deleteFlight(flight.getFlightId());
                    loadFlights();
                    showSuccess("Flight deleted successfully!");
                } catch (SQLException e) {
                    showError("Failed to delete flight: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}