package com.airline.reservation.ui.admin;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.airline.reservation.model.User;
import com.airline.reservation.model.enums.UserRole;
import com.airline.reservation.service.UserService;
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

public class UserManagementPanel extends VBox {
    private final UserService userService;
    private TableView<User> userTable;
    private ObservableList<User> userData;
    private TextField searchField;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public UserManagementPanel() {
        this.userService = new UserService();
        this.userData = FXCollections.observableArrayList();
        initializeComponents();
        loadUsers();
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
        Label titleLabel = new Label("User Management");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.TEXT_PRIMARY + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search users...");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(StyleConstants.INPUT_HEIGHT);
        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));

        // Add button
        Button addButton = new Button("Add User", new FontIcon(MaterialDesignA.ACCOUNT_PLUS));
        addButton.getStyleClass().addAll(Styles.ACCENT);
        addButton.setPrefHeight(StyleConstants.BUTTON_HEIGHT);
        addButton.setOnAction(e -> showAddUserDialog());

        // Refresh button
        Button refreshButton = new Button("", new FontIcon(MaterialDesignR.REFRESH));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        refreshButton.setPrefHeight(StyleConstants.BUTTON_HEIGHT);
        refreshButton.setOnAction(e -> loadUsers());

        HBox buttonBox = new HBox(StyleConstants.SPACING_SM, searchField, addButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(titleLabel, spacer, buttonBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, StyleConstants.SPACING_MD, 0));

        return header;
    }

    private VBox createTableView() {
        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        // Columns
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        idCol.setPrefWidth(60);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<User, UserRole> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(col -> new TableCell<User, UserRole>() {
            @Override
            protected void updateItem(UserRole item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    String color = switch (item) {
                        case ADMIN -> StyleConstants.ERROR_COLOR;
                        case STAFF -> StyleConstants.WARNING_COLOR;
                        case PASSENGER -> StyleConstants.PRIMARY_COLOR;
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<User, LocalDateTime> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdCol.setCellFactory(col -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("", new FontIcon(MaterialDesignP.PENCIL));
            private final Button deleteBtn = new Button("", new FontIcon(MaterialDesignD.DELETE));
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
                deleteBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
                pane.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        userTable.getColumns().addAll(idCol, usernameCol, emailCol, fullNameCol, roleCol, createdCol, actionsCol);
        userTable.setItems(userData);

        return new VBox(userTable);
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            userData.setAll(users);
        } catch (SQLException e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadUsers();
            return;
        }
        
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> filtered = allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(searchText.toLowerCase()) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchText.toLowerCase())) ||
                           (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchText.toLowerCase())))
                .toList();
            userData.setAll(filtered);
        } catch (SQLException e) {
            showError("Failed to filter users: " + e.getMessage());
        }
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter user details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = createUserForm(null);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return extractUserFromForm(grid, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            try {
                userService.createUser(user);
                loadUsers();
                showSuccess("User added successfully!");
            } catch (SQLException e) {
                showError("Failed to add user: " + e.getMessage());
            }
        });
    }

    private void showEditUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Update user details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = createUserForm(user);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return extractUserFromForm(grid, user);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedUser -> {
            try {
                userService.updateUser(updatedUser);
                loadUsers();
                showSuccess("User updated successfully!");
            } catch (SQLException e) {
                showError("Failed to update user: " + e.getMessage());
            }
        });
    }

    private GridPane createUserForm(User user) {
        GridPane grid = new GridPane();
        grid.setHgap(StyleConstants.SPACING_SM);
        grid.setVgap(StyleConstants.SPACING_SM);
        grid.setPadding(StyleConstants.PADDING_MD);

        TextField usernameField = new TextField(user != null ? user.getUsername() : "");
        PasswordField passwordField = new PasswordField();
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        TextField fullNameField = new TextField(user != null ? user.getFullName() : "");
        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.setValue(user != null ? user.getRole() : UserRole.PASSENGER);

        usernameField.setPromptText("Username");
        passwordField.setPromptText(user != null ? "Leave empty to keep current password" : "Password");
        emailField.setPromptText("email@example.com");
        fullNameField.setPromptText("Full Name");

        if (user != null) {
            usernameField.setDisable(true); // Don't allow username changes
        }

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Full Name:"), 0, 3);
        grid.add(fullNameField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        return grid;
    }

    private User extractUserFromForm(GridPane grid, User existingUser) {
        TextField usernameField = (TextField) grid.getChildren().get(1);
        PasswordField passwordField = (PasswordField) grid.getChildren().get(3);
        TextField emailField = (TextField) grid.getChildren().get(5);
        TextField fullNameField = (TextField) grid.getChildren().get(7);
        @SuppressWarnings("unchecked")
        ComboBox<UserRole> roleCombo = (ComboBox<UserRole>) grid.getChildren().get(9);

        User user = existingUser != null ? existingUser : new User();
        user.setUsername(usernameField.getText());
        
        // Only update password if it's not empty
        if (!passwordField.getText().isEmpty()) {
            user.setPassword(passwordField.getText());
        }
        
        user.setEmail(emailField.getText());
        user.setFullName(fullNameField.getText());
        user.setRole(roleCombo.getValue());

        return user;
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Delete user " + user.getUsername() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(user.getUserId());
                    loadUsers();
                    showSuccess("User deleted successfully!");
                } catch (SQLException e) {
                    showError("Failed to delete user: " + e.getMessage());
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