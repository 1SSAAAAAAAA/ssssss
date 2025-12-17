package com.airline.reservation.ui.staff;

import com.airline.reservation.model.Flight;
import com.airline.reservation.service.FlightService;
import com.airline.reservation.ui.components.StyleConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightViewPanel extends JPanel {
    private final FlightService flightService;
    private JTable flightTable;
    private DefaultTableModel tableModel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FlightViewPanel() {
        this.flightService = new FlightService();
        
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BACKGROUND_LIGHT_GRAY);
        
        initializeComponents();
        loadFlights();
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
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(StyleConstants.FONT_REGULAR);
        refreshButton.setBackground(StyleConstants.PRIMARY_BLUE);
        refreshButton.setForeground(StyleConstants.WHITE);
        refreshButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadFlights());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(StyleConstants.WHITE);
        buttonPanel.add(refreshButton);
        
        topPanel.add(buttonPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Flight Number", "Origin", "Destination", "Departure", "Arrival", "Total Seats", "Available", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        flightTable = new JTable(tableModel);
        flightTable.setFont(StyleConstants.FONT_REGULAR);
        flightTable.setRowHeight(StyleConstants.SPACING_LG);
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.getTableHeader().setFont(StyleConstants.FONT_REGULAR);
        flightTable.getTableHeader().setBackground(StyleConstants.PRIMARY_BLUE);
        flightTable.getTableHeader().setForeground(StyleConstants.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD
        ));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadFlights() {
        try {
            List<Flight> flights = flightService.getAllFlights();
            tableModel.setRowCount(0);
            
            for (Flight flight : flights) {
                int available = flightService.getAvailableSeatsCount(flight.getFlightId());
                tableModel.addRow(new Object[]{
                    flight.getFlightId(),
                    flight.getFlightNumber(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getDepartureTime().format(formatter),
                    flight.getArrivalTime().format(formatter),
                    flight.getTotalSeats(),
                    available,
                    flight.getStatus()
                });
            }
        } catch (SQLException ex) {
            showError("Failed to load flights: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}