package com.airline.reservation.ui.components;

import com.airline.reservation.model.enums.UserRole;
import com.airline.reservation.service.AuthenticationService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;

import java.util.function.Consumer;

public class SidebarView extends VBox {
    private final AuthenticationService authService;
    private Consumer<String> onNavigate;

    public SidebarView(AuthenticationService authService) {
        this.authService = authService;
        initializeComponents();
    }

    private void initializeComponents() {
        setSpacing(StyleConstants.SPACING_SM);
        setPadding(StyleConstants.PADDING_MD);
        setPrefWidth(StyleConstants.SIDEBAR_WIDTH);
        setStyle("-fx-background-color: " + StyleConstants.CARD_BACKGROUND + ";" +
                 "-fx-border-color: " + StyleConstants.BORDER_COLOR + ";" +
                 "-fx-border-width: 0 1 0 0;");

        // Header
        Label titleLabel = new Label("Flight System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + StyleConstants.PRIMARY_COLOR + ";");
        
        Label userLabel = new Label("User: " + authService.getCurrentUser().getUsername());
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleConstants.TEXT_SECONDARY + ";");
        
        Label roleLabel = new Label("Role: " + authService.getCurrentUser().getRole().toString());
        roleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleConstants.TEXT_SECONDARY + ";");

        VBox header = new VBox(4, titleLabel, userLabel, roleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Separator separator1 = new Separator();
        
        // Navigation buttons based on role
        VBox navButtons = new VBox(StyleConstants.SPACING_XS);
        UserRole role = authService.getCurrentUser().getRole();

        if (role == UserRole.ADMIN) {
            navButtons.getChildren().addAll(
                createNavButton("Flight Management", new FontIcon(MaterialDesignA.AIRPLANE), "flights"),
                createNavButton("User Management", new FontIcon(MaterialDesignA.ACCOUNT_GROUP), "users"),
                createNavButton("Bookings", new FontIcon(MaterialDesignB.BOOK), "bookings"),
                createNavButton("Passenger List", new FontIcon(MaterialDesignP.PASSPORT), "passengers")
            );
        } else if (role == UserRole.STAFF) {
            navButtons.getChildren().addAll(
                createNavButton("View Flights", new FontIcon(MaterialDesignA.AIRPLANE), "view-flights"),
                createNavButton("Bookings", new FontIcon(MaterialDesignB.BOOK), "bookings"),
                createNavButton("Manage Passengers", new FontIcon(MaterialDesignP.PASSPORT), "manage-passengers")
            );
        } else if (role == UserRole.PASSENGER) {
            navButtons.getChildren().addAll(
                createNavButton("Search Flights", new FontIcon(MaterialDesignA.AIRPLANE), "search"),
                createNavButton("My Bookings", new FontIcon(MaterialDesignB.BOOK_OPEN_PAGE_VARIANT), "my-bookings"),
                createNavButton("Create Booking", new FontIcon(MaterialDesignB.BOOK_PLUS), "create-booking")
            );
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout button
        Button logoutButton = createNavButton("Logout", new FontIcon(MaterialDesignL.LOGOUT), "logout");
        logoutButton.setStyle(logoutButton.getStyle() + "-fx-text-fill: " + StyleConstants.ERROR_COLOR + ";");

        getChildren().addAll(header, separator1, navButtons, spacer, logoutButton);
    }

    private Button createNavButton(String text, FontIcon icon, String action) {
        Button button = new Button(text, icon);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(12);
        button.setPrefHeight(40);
        button.getStyleClass().addAll("button", "flat");
        
        icon.setIconSize(20);
        icon.setIconColor(javafx.scene.paint.Color.web(StyleConstants.TEXT_SECONDARY));
        
        button.setStyle("-fx-background-color: transparent;" +
                       "-fx-text-fill: " + StyleConstants.TEXT_PRIMARY + ";" +
                       "-fx-font-size: 14px;" +
                       "-fx-cursor: hand;" +
                       "-fx-background-radius: " + StyleConstants.BORDER_RADIUS_SM + ";");
        
        button.setOnMouseEntered(e -> 
            button.setStyle(button.getStyle() + "-fx-background-color: " + StyleConstants.BACKGROUND_COLOR + ";")
        );
        button.setOnMouseExited(e -> 
            button.setStyle(button.getStyle().replace("-fx-background-color: " + StyleConstants.BACKGROUND_COLOR + ";", "-fx-background-color: transparent;"))
        );
        
        button.setOnAction(e -> {
            if (onNavigate != null) {
                onNavigate.accept(action);
            }
        });
        
        return button;
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }
}