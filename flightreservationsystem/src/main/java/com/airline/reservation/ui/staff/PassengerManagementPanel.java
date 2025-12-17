package com.airline.reservation.ui.staff;

import com.airline.reservation.model.Passenger;
import com.airline.reservation.service.PassengerService;
import com.airline.reservation.ui.components.StyleConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PassengerManagementPanel extends JPanel {
    private final PassengerService passengerService;
    private JTable passengerTable;
    private DefaultTableModel tableModel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PassengerManagementPanel() {
        this.passengerService = new PassengerService();
        
        setLayout(new BorderLayout());
        setBackground(StyleConstants.BACKGROUND_LIGHT_GRAY);
        
        initializeComponents();
        loadPassengers();
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
        
        JButton createButton = new JButton("Add Passenger");
        createButton.setFont(StyleConstants.FONT_REGULAR);
        createButton.setBackground(StyleConstants.PRIMARY_BLUE);
        createButton.setForeground(StyleConstants.WHITE);
        createButton.setPreferredSize(new Dimension(160, StyleConstants.BUTTON_HEIGHT));
        createButton.setFocusPainted(false);
        createButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createButton.addActionListener(e -> showCreatePassengerDialog());
        
        JButton editButton = new JButton("Edit Passenger");
        editButton.setFont(StyleConstants.FONT_REGULAR);
        editButton.setBackground(StyleConstants.ACTION_ORANGE);
        editButton.setForeground(StyleConstants.WHITE);
        editButton.setPreferredSize(new Dimension(160, StyleConstants.BUTTON_HEIGHT));
        editButton.setFocusPainted(false);
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> showEditPassengerDialog());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(StyleConstants.FONT_REGULAR);
        refreshButton.setBackground(StyleConstants.SECONDARY_TEAL);
        refreshButton.setForeground(StyleConstants.WHITE);
        refreshButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadPassengers());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, StyleConstants.SPACING_SM, 0));
        buttonPanel.setBackground(StyleConstants.WHITE);
        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(refreshButton);
        
        topPanel.add(buttonPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Full Name", "Date of Birth", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        passengerTable = new JTable(tableModel);
        passengerTable.setFont(StyleConstants.FONT_REGULAR);
        passengerTable.setRowHeight(StyleConstants.SPACING_LG);
        passengerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passengerTable.getTableHeader().setFont(StyleConstants.FONT_REGULAR);
        passengerTable.getTableHeader().setBackground(StyleConstants.PRIMARY_BLUE);
        passengerTable.getTableHeader().setForeground(StyleConstants.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(passengerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD
        ));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadPassengers() {
        try {
            List<Passenger> passengers = passengerService.getAllPassengers();
            tableModel.setRowCount(0);
            
            for (Passenger passenger : passengers) {
                tableModel.addRow(new Object[]{
                    passenger.getPassengerId(),
                    passenger.getFullName(),
                    passenger.getDateOfBirth().format(formatter),
                    passenger.getEmail(),
                    passenger.getPhone()
                });
            }
        } catch (SQLException ex) {
            showError("Failed to load passengers: " + ex.getMessage());
        }
    }

    private void showCreatePassengerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Passenger", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(StyleConstants.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD,
            StyleConstants.SPACING_MD
        ));
        
        JTextField nameField = createTextField();
        JTextField dobField = createTextField();
        JTextField emailField = createTextField();
        JTextField phoneField = createTextField();
        
        addFormField(formPanel, "Full Name:", nameField);
        addFormField(formPanel, "Date of Birth (yyyy-MM-dd):", dobField);
        addFormField(formPanel, "Email:", emailField);
        addFormField(formPanel, "Phone:", phoneField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(StyleConstants.WHITE);
        
        JButton saveButton = new JButton("Add");
        saveButton.setFont(StyleConstants.FONT_REGULAR);
        saveButton.setBackground(StyleConstants.SUCCESS_GREEN);
        saveButton.setForeground(StyleConstants.WHITE);
        saveButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> {
            try {
                Passenger passenger = new Passenger();
                passenger.setFullName(nameField.getText().trim());
                passenger.setDateOfBirth(LocalDate.parse(dobField.getText().trim(), formatter));
                passenger.setEmail(emailField.getText().trim());
                passenger.setPhone(phoneField.getText().trim());
                
                if (passengerService.createPassenger(passenger)) {
                    showSuccess("Passenger added successfully");
                    loadPassengers();
                    dialog.dispose();
                } else {
                    showError("Failed to add passenger");
                }
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(StyleConstants.FONT_REGULAR);
        cancelButton.setBackground(StyleConstants.BORDER_GRAY);
        cancelButton.setForeground(StyleConstants.TEXT_DARK_GRAY);
        cancelButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditPassengerDialog() {
        int selectedRow = passengerTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a passenger to edit");
            return;
        }
        
        int passengerId = (int) tableModel.getValueAt(selectedRow, 0);
        
        try {
            Passenger passenger = passengerService.getPassengerById(passengerId);
            if (passenger == null) {
                showError("Passenger not found");
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Passenger", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(480, 480);
            dialog.setLocationRelativeTo(this);
            
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(StyleConstants.WHITE);
            formPanel.setBorder(BorderFactory.createEmptyBorder(
                StyleConstants.SPACING_MD,
                StyleConstants.SPACING_MD,
                StyleConstants.SPACING_MD,
                StyleConstants.SPACING_MD
            ));
            
            JTextField nameField = createTextField();
            nameField.setText(passenger.getFullName());
            
            JTextField dobField = createTextField();
            dobField.setText(passenger.getDateOfBirth().format(formatter));
            
            JTextField emailField = createTextField();
            emailField.setText(passenger.getEmail());
            
            JTextField phoneField = createTextField();
            phoneField.setText(passenger.getPhone());
            
            addFormField(formPanel, "Full Name:", nameField);
            addFormField(formPanel, "Date of Birth (yyyy-MM-dd):", dobField);
            addFormField(formPanel, "Email:", emailField);
            addFormField(formPanel, "Phone:", phoneField);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(StyleConstants.WHITE);
            
            JButton saveButton = new JButton("Update");
            saveButton.setFont(StyleConstants.FONT_REGULAR);
            saveButton.setBackground(StyleConstants.SUCCESS_GREEN);
            saveButton.setForeground(StyleConstants.WHITE);
            saveButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
            saveButton.setFocusPainted(false);
            saveButton.addActionListener(e -> {
                try {
                    passenger.setFullName(nameField.getText().trim());
                    passenger.setDateOfBirth(LocalDate.parse(dobField.getText().trim(), formatter));
                    passenger.setEmail(emailField.getText().trim());
                    passenger.setPhone(phoneField.getText().trim());
                    
                    if (passengerService.updatePassenger(passenger)) {
                        showSuccess("Passenger updated successfully");
                        loadPassengers();
                        dialog.dispose();
                    } else {
                        showError("Failed to update passenger");
                    }
                } catch (Exception ex) {
                    showError("Error: " + ex.getMessage());
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(StyleConstants.FONT_REGULAR);
            cancelButton.setBackground(StyleConstants.BORDER_GRAY);
            cancelButton.setForeground(StyleConstants.TEXT_DARK_GRAY);
            cancelButton.setPreferredSize(new Dimension(120, StyleConstants.BUTTON_HEIGHT));
            cancelButton.setFocusPainted(false);
            cancelButton.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            
            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
            
        } catch (SQLException ex) {
            showError("Failed to load passenger: " + ex.getMessage());
        }
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(StyleConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, StyleConstants.INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, StyleConstants.INPUT_HEIGHT));
        return field;
    }

    private void addFormField(JPanel panel, String label, JComponent field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(StyleConstants.FONT_REGULAR);
        jLabel.setForeground(StyleConstants.TEXT_DARK_GRAY);
        jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
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
}