package com.airline.reservation.ui.passenger;

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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyBookingsPanel extends JPanel {
    private final BookingService bookingService;
    private final FlightService flightService;
    private final PassengerService passengerService;
    private final AuthenticationService authService;
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public MyBookingsPanel(AuthenticationService authService) {
        this.bookingService = new BookingService();
        this.flightService = new FlightService();
        this.passengerService = new PassengerService();
        this.authService = authService;
        
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BACKGROUND_LIGHT_GRAY);
        
        initializeComponents();
        loadMyBookings();
    }

    private void initializeComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(StyleConstants.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD
        ));
        
        JButton viewButton = new JButton("View Details");
        viewButton.setFont(StyleConstants.FONT_REGULAR);
        viewButton.setBackground(StyleConstants.SECONDARY_TEAL);
        viewButton.setForeground(StyleConstants.WHITE);
        viewButton.setPreferredSize(new Dimension(160, StyleConstants.BUTTON_HEIGHT));
        viewButton.setFocusPainted(false);
        viewButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> viewBookingDetails());
        
        JButton cancelButton = new JButton("Cancel Booking");
        cancelButton.setFont(StyleConstants.FONT_REGULAR);
        cancelButton.setBackground(StyleConstants.ERROR_RED);
        cancelButton.setForeground(StyleConstants.WHITE);
        cancelButton.setPreferredSize(new Dimension(160, StyleConstants.BUTTON_HEIGHT));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> cancelBooking());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(StyleConstants.FONT_REGULAR);
        refreshButton.setBackground(StyleConstants.PRIMARY_BLUE);
        refreshButton.setForeground(StyleConstants.WHITE);
        refreshButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadMyBookings());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, StyleConstants.SPACING_SM, 0));
        buttonPanel.setBackground(StyleConstants.WHITE);
        buttonPanel.add(viewButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);
        
        topPanel.add(buttonPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"Booking Reference", "Flight Number", "Route", "Departure", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookingTable = new JTable(tableModel);
        bookingTable.setFont(StyleConstants.FONT_REGULAR);
        bookingTable.setRowHeight(StyleConstants.SPACING_LG);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getTableHeader().setFont(StyleConstants.FONT_REGULAR);
        bookingTable.getTableHeader().setBackground(StyleConstants.PRIMARY_BLUE);
        bookingTable.getTableHeader().setForeground(StyleConstants.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD
        ));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadMyBookings() {
        try {
            int userId = authService.getCurrentUser().getUserId();
            List<Booking> bookings = bookingService.getBookingsByUserId(userId);
            tableModel.setRowCount(0);
            
            for (Booking booking : bookings) {
                Flight flight = flightService.getFlightById(booking.getFlightId());
                if (flight != null) {
                    tableModel.addRow(new Object[]{
                        booking.getBookingReference(),
                        flight.getFlightNumber(),
                        flight.getOrigin() + " to " + flight.getDestination(),
                        flight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        booking.getStatus()
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load bookings: " + ex.getMessage());
        }
    }

    private void viewBookingDetails() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to view");
            return;
        }
        
        String bookingReference = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            Booking booking = bookingService.getBookingByReference(bookingReference);
            Flight flight = flightService.getFlightById(booking.getFlightId());
            List<Passenger> passengers = passengerService.getPassengersByBookingId(booking.getBookingId());
            List<Seat> seats = bookingService.getBookingSeats(booking.getBookingId());
            
            StringBuilder details = new StringBuilder();
            details.append("Booking Reference: ").append(booking.getBookingReference()).append("\n");
            details.append("Flight: ").append(flight.getFlightNumber()).append("\n");
            details.append("Route: ").append(flight.getOrigin()).append(" to ").append(flight.getDestination()).append("\n");
            details.append("Departure: ").append(flight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            details.append("Arrival: ").append(flight.getArrivalTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            details.append("Status: ").append(booking.getStatus()).append("\n\n");
            details.append("Passengers:\n");
            
            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                details.append("  - ").append(p.getFullName());
                if (i < seats.size()) {
                    details.append(" (Seat: ").append(seats.get(i).getSeatNumber()).append(")");
                }
                details.append("\n");
            }
            
            JOptionPane.showMessageDialog(this, details.toString(), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException ex) {
            showError("Failed to load booking details: " + ex.getMessage());
        }
    }

    private void cancelBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to cancel");
            return;
        }
        
        String bookingReference = (String) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this booking?",
            "Confirm Cancel",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Booking booking = bookingService.getBookingByReference(bookingReference);
                if (bookingService.cancelBooking(booking.getBookingId())) {
                    showSuccess("Booking cancelled successfully");
                    loadMyBookings();
                } else {
                    showError("Failed to cancel booking");
                }
            } catch (SQLException ex) {
                showError("Error: " + ex.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}