package com.airline.reservation.ui.components;

import javafx.geometry.Insets;

public class StyleConstants {
    // AtlantaFX Primer Theme Colors
    public static final String PRIMARY_COLOR = "#0969da";      // Primer blue
    public static final String SECONDARY_COLOR = "#1f883d";    // Primer green
    public static final String SUCCESS_COLOR = "#1a7f37";      // Success green
    public static final String ERROR_COLOR = "#cf222e";        // Error red
    public static final String WARNING_COLOR = "#bf8700";      // Warning orange
    public static final String BACKGROUND_COLOR = "#f6f8fa";   // Light background
    public static final String CARD_BACKGROUND = "#ffffff";    // White cards
    public static final String BORDER_COLOR = "#d0d7de";       // Border gray
    public static final String TEXT_PRIMARY = "#24292f";       // Primary text
    public static final String TEXT_SECONDARY = "#57606a";     // Secondary text
    
    // Spacing
    public static final double SPACING_XS = 8;
    public static final double SPACING_SM = 16;
    public static final double SPACING_MD = 24;
    public static final double SPACING_LG = 32;
    public static final double SPACING_XL = 48;
    
    // Sizes
    public static final double SIDEBAR_WIDTH = 240;
    public static final double BUTTON_HEIGHT = 40;
    public static final double INPUT_HEIGHT = 40;
    public static final double TABLE_ROW_HEIGHT = 45;
    
    // Insets
    public static final Insets PADDING_XS = new Insets(SPACING_XS);
    public static final Insets PADDING_SM = new Insets(SPACING_SM);
    public static final Insets PADDING_MD = new Insets(SPACING_MD);
    public static final Insets PADDING_LG = new Insets(SPACING_LG);
    
    // Border Radius
    public static final String BORDER_RADIUS_SM = "6px";
    public static final String BORDER_RADIUS_MD = "8px";
    public static final String BORDER_RADIUS_LG = "12px";
    
    // Shadows
    public static final String SHADOW_SM = "dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1)";
    public static final String SHADOW_MD = "dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2)";
    public static final String SHADOW_LG = "dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4)";
    
    private StyleConstants() {}
    
    // Helper methods for consistent styling
    public static String cardStyle() {
        return "-fx-background-color: " + CARD_BACKGROUND + ";" +
               "-fx-background-radius: " + BORDER_RADIUS_MD + ";" +
               "-fx-effect: " + SHADOW_SM + ";";
    }
    
    public static String buttonPrimaryStyle() {
        return "-fx-background-color: " + PRIMARY_COLOR + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: " + BORDER_RADIUS_SM + ";" +
               "-fx-padding: 8 16 8 16;" +
               "-fx-cursor: hand;";
    }
    
    public static String buttonSecondaryStyle() {
        return "-fx-background-color: " + SECONDARY_COLOR + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: " + BORDER_RADIUS_SM + ";" +
               "-fx-padding: 8 16 8 16;" +
               "-fx-cursor: hand;";
    }
    
    public static String buttonDangerStyle() {
        return "-fx-background-color: " + ERROR_COLOR + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: " + BORDER_RADIUS_SM + ";" +
               "-fx-padding: 8 16 8 16;" +
               "-fx-cursor: hand;";
    }
}