package com.airline.reservation;

import atlantafx.base.theme.PrimerLight;
import com.airline.reservation.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView, 480, 600);
        
        primaryStage.setTitle("Flight Reservation System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}