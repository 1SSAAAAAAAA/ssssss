package com.airline.reservation.ui;

import atlantafx.base.controls.PasswordTextField;
import com.airline.reservation.model.User;
import com.airline.reservation.service.AuthenticationService;
import com.airline.reservation.ui.components.StyleConstants;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginView extends VBox {
    private final AuthenticationService authService;
    private final Stage primaryStage;
    private TextField usernameField;
    private PasswordTextField passwordField;

    public LoginView(Stage primaryStage) {
        this.authService = new AuthenticationService();
        this.primaryStage = primaryStage;
        
        initializeComponents();
    }

    private void initializeComponents() {
        setAlignment(Pos.CENTER);
        setSpacing(0);
        setStyle("-fx-background-color: " + StyleConstants.BACKGROUND_COLOR + ";");
        
        // Main card container
        VBox card = new VBox(StyleConstants.SPACING_MD);
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(400);
        card.setPadding(StyleConstants.PADDING_LG);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        // Title
        Label titleLabel = new Label("Flight Reservation System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.PRIMARY_COLOR + ";");
        
        Label subtitleLabel = new Label("Login to continue");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        
        // Form fields with proper spacing
        VBox formContainer = new VBox(StyleConstants.SPACING_SM);
        formContainer.setPadding(new javafx.geometry.Insets(StyleConstants.SPACING_MD, 0, 0, 0));
        
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500;");
        
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(StyleConstants.INPUT_HEIGHT);
        usernameField.getStyleClass().add("text-input");
        
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500;");
        
        passwordField = new PasswordTextField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(StyleConstants.INPUT_HEIGHT);
        passwordField.setOnAction(e -> performLogin());
        
        formContainer.getChildren().addAll(
            usernameLabel, usernameField,
            passwordLabel, passwordField
        );
        
        // Login button
        Button loginButton = new Button("Login");
        loginButton.setPrefHeight(StyleConstants.BUTTON_HEIGHT);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.getStyleClass().addAll("button", "accent");
        loginButton.setOnAction(e -> performLogin());
        
        // Info label
        Label infoLabel = new Label("Default credentials:\nadmin/admin123, staff1/staff123, passenger1/pass123");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-text-alignment: center;");
        infoLabel.setWrapText(true);
        infoLabel.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            formContainer,
            loginButton,
            infoLabel
        );
        
        getChildren().add(card);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                MainView mainView = new MainView(authService);
                primaryStage.getScene().setRoot(mainView);
                primaryStage.setWidth(1280);
                primaryStage.setHeight(800);
                primaryStage.centerOnScreen();
                primaryStage.setResizable(true);
                primaryStage.setTitle("Flight Reservation System");
            } else {
                showError("Invalid username or password");
                passwordField.clear();
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}