package com.airline.reservation.ui.passenger;

import com.airline.reservation.dao.SeatDAO;
import com.airline.reservation.model.Booking;
import com.airline.reservation.model.Flight;
import com.airline.reservation.model.Passenger;
import com.airline.reservation.model.Seat;
import com.airline.reservation.service.AuthenticationService;
import com.airline.reservation.service.BookingService;
import com.airline.reservation.service.FlightService;
import com.airline.reservation.service.PassengerService;
import com.airline.reservation.ui.components.StyleConstants;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingCreationPanel extends JPanel {
    private final BookingService bookingService;
    private final FlightService flightService;
    private final PassengerService passengerService;
    private final AuthenticationService authService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private JComboBox<FlightItem> flightCombo;
    private JTextField passengerNameField;
    private JTextField dobField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<SeatItem> seatCombo;
    private JTextArea confirmationArea;

    public BookingCreationPanel(AuthenticationService authService) {
        this.bookingService = new BookingService();
        this.flightService = new FlightService();
        this.passengerService = new PassengerService();
        this.authService = authService;
        
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BACKGROUND_LIGHT_GRAY);
        
        initializeComponents();
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(StyleConstants.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_LG,
            StyleConstants.SPACING_LG,
            StyleConstants.SPACING_LG,
            StyleConstants.SPACING_LG
        ));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(StyleConstants.WHITE);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        flightCombo = new JComboBox<>();
        flightCombo.setFont(StyleConstants.FONT_REGULAR);
        flightCombo.setMaximumSize(new Dimension(600, StyleConstants.INPUT_HEIGHT));
        flightCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        try {
            List<Flight> flights = flightService.getAllFlights();
            for (Flight flight : flights) {
                if (flight.getStatus().name().equals("SCHEDULED")) {
                    flightCombo.addItem(new FlightItem(flight));
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load flights: " + ex.getMessage());
        }
        
        passengerNameField = createTextField();
        dobField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();
        
        seatCombo = new JComboBox<>();
        seatCombo.setFont(StyleConstants.FONT_REGULAR);
        seatCombo.setMaximumSize(new Dimension(600, StyleConstants.INPUT_HEIGHT));
        seatCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        flightCombo.addActionListener(e -> loadAvailableSeats());
        
        addFormField(formPanel, "Select Flight:", flightCombo);
        addFormField(formPanel, "Passenger Name:", passengerNameField);
        addFormField(formPanel, "Date of Birth (yyyy-MM-dd):", dobField);
        addFormField(formPanel, "Email:", emailField);
        addFormField(formPanel, "Phone:", phoneField);
        addFormField(formPanel, "Select Seat:", seatCombo);
        
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(StyleConstants.SPACING_LG));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, StyleConstants.SPACING_SM, 0));
        buttonPanel.setBackground(StyleConstants.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton bookButton = new JButton("Book Flight");
        bookButton.setFont(StyleConstants.FONT_REGULAR);
        bookButton.setBackground(StyleConstants.SUCCESS_GREEN);
        bookButton.setForeground(StyleConstants.WHITE);
        bookButton.setPreferredSize(new Dimension(160, StyleConstants.BUTTON_HEIGHT));
        bookButton.setFocusPainted(false);
        bookButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookButton.addActionListener(e -> createBooking());
        
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(StyleConstants.FONT_REGULAR);
        clearButton.setBackground(StyleConstants.BORDER_GRAY);
        clearButton.setForeground(StyleConstants.TEXT_DARK_GRAY);
        clearButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);
        mainPanel.add(buttonPanel);
        
        mainPanel.add(Box.createVerticalStrut(StyleConstants.SPACING_LG));
        
        JLabel confirmationLabel = new JLabel("Booking Confirmation:");
        confirmationLabel.setFont(StyleConstants.FONT_MEDIUM);
        confirmationLabel.setForeground(StyleConstants.TEXT_DARK_GRAY);
        confirmationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(confirmationLabel);
        
        mainPanel.add(Box.createVerticalStrut(StyleConstants.SPACING_XS));
        
        confirmationArea = new JTextArea();
        confirmationArea.setFont(StyleConstants.FONT_REGULAR);
        confirmationArea.setEditable(false);
        confirmationArea.setBackground(StyleConstants.BACKGROUND_LIGHT_GRAY);
        confirmationArea.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_SM,
            StyleConstants.SPACING_SM,
            StyleConstants.SPACING_SM,
            StyleConstants.SPACING_SM
        ));
        
        JScrollPane confirmationScroll = new JScrollPane(confirmationArea);
        confirmationScroll.setPreferredSize(new Dimension(600, 200));
        confirmationScroll.setMaximumSize(new Dimension(600, 200));
        confirmationScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(confirmationScroll);
        
        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD
        ));
        add(mainScroll, BorderLayout.CENTER);
        
        loadAvailableSeats();
    }

    private void loadAvailableSeats() {
        FlightItem selectedFlight = (FlightItem) flightCombo.getSelectedItem();
        if (selectedFlight != null) {
            try {
                seatCombo.removeAllItems();
                SeatDAO seatDAO = new SeatDAO();
                List<Seat> availableSeats = seatDAO.findAvailableSeatsByFlightId(
                    selectedFlight.getFlight().getFlightId()
                );
                
                for (Seat seat : availableSeats) {
                    seatCombo.addItem(new SeatItem(seat));
                }
            } catch (SQLException ex) {
                showError("Failed to load seats: " + ex.getMessage());
            }
        }
    }

    private void createBooking() {
        FlightItem selectedFlight = (FlightItem) flightCombo.getSelectedItem();
        SeatItem selectedSeat = (SeatItem) seatCombo.getSelectedItem();
        
        if (selectedFlight == null) {
            showError("Please select a flight");
            return;
        }
        
        if (selectedSeat == null) {
            showError("No seats available for this flight");
            return;
        }
        
        String name = passengerNameField.getText().trim();
        String dob = dobField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (name.isEmpty() || dob.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all passenger details");
            return;
        }
        
        try {
            Passenger passenger = new Passenger();
            passenger.setFullName(name);
            passenger.setDateOfBirth(LocalDate.parse(dob, formatter));
            passenger.setEmail(email);
            passenger.setPhone(phone);
            
            if (passengerService.createPassenger(passenger)) {
                String bookingRef = bookingService.createBooking(
                    selectedFlight.getFlight().getFlightId(),
                    authService.getCurrentUser().getUserId()
                );
                
                Booking booking = bookingService.getBookingByReference(bookingRef);
                
                if (bookingService.assignSeatToBooking(
                    booking.getBookingId(),
                    selectedSeat.getSeat().getSeatId(),
                    passenger.getPassengerId()
                )) {
                    displayConfirmation(booking, selectedFlight.getFlight(), passenger, selectedSeat.getSeat());
                    showSuccess("Booking created successfully!");
                    clearForm();
                    loadAvailableSeats();
                } else {
                    showError("Failed to assign seat");
                }
            } else {
                showError("Failed to create passenger record");
            }
        } catch (Exception ex) {
            showError("Booking error: " + ex.getMessage());
        }
    }

    private void displayConfirmation(Booking booking, Flight flight, Passenger passenger, Seat seat) {
        StringBuilder confirmation = new StringBuilder();
        confirmation.append("Booking Successful!\n\n");
        confirmation.append("Booking Reference: ").append(booking.getBookingReference()).append("\n");
        confirmation.append("Flight: ").append(flight.getFlightNumber()).append("\n");
        confirmation.append("Route: ").append(flight.getOrigin()).append(" to ").append(flight.getDestination()).append("\n");
        confirmation.append("Departure: ").append(flight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        confirmation.append("Arrival: ").append(flight.getArrivalTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        confirmation.append("Passenger: ").append(passenger.getFullName()).append("\n");
        confirmation.append("Seat: ").append(seat.getSeatNumber()).append("\n\n");
        confirmation.append("Please save your booking reference for future reference.");
        
        confirmationArea.setText(confirmation.toString());
    }

    private void clearForm() {
        passengerNameField.setText("");
        dobField.setText("");
        emailField.setText("");
        phoneField.setText("");
        confirmationArea.setText("");
        if (flightCombo.getItemCount() > 0) {
            flightCombo.setSelectedIndex(0);
        }
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(StyleConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, StyleConstants.INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(600, StyleConstants.INPUT_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void addFormField(JPanel panel, String label, JComponent field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(StyleConstants.FONT_REGULAR);
        jLabel.setForeground(StyleConstants.TEXT_DARK_GRAY);
        jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(jLabel);
        panel.add(Box.createVerticalStrut(StyleConstants.SPACING_XS));
        panel.add(field);
        panel.add(Box.createVerticalStrut(StyleConstants.SPACING_MD));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class FlightItem {
        private final Flight flight;

        public FlightItem(Flight flight) {
            this.flight = flight;
        }

        public Flight getFlight() {
            return flight;
        }

        @Override
        public String toString() {
            return flight.getFlightNumber() + " - " + 
                   flight.getOrigin() + " to " + flight.getDestination() + 
                   " (" + flight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ")";
        }
    }

    private static class SeatItem {
        private final Seat seat;

        public SeatItem(Seat seat) {
            this.seat = seat;
        }

        public Seat getSeat() {
            return seat;
        }

        @Override
        public String toString() {
            return seat.getSeatNumber();
        }
    }
}