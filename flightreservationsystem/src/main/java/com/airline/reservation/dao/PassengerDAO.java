package com.airline.reservation.dao;

import com.airline.reservation.config.DatabaseConnection;
import com.airline.reservation.model.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    public Passenger findById(int passengerId) throws SQLException {
        String query = "SELECT * FROM passengers WHERE passenger_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, passengerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPassengerFromResultSet(rs);
            }
        }
        return null;
    }

    public List<Passenger> findAll() throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String query = "SELECT * FROM passengers ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                passengers.add(extractPassengerFromResultSet(rs));
            }
        }
        return passengers;
    }

    public List<Passenger> findByFlightId(int flightId) throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String query = "SELECT DISTINCT p.* FROM passengers p " +
                      "INNER JOIN seats s ON p.passenger_id = s.passenger_id " +
                      "WHERE s.flight_id = ? AND s.passenger_id IS NOT NULL " +
                      "ORDER BY p.full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                passengers.add(extractPassengerFromResultSet(rs));
            }
        }
        return passengers;
    }

    public List<Passenger> findByBookingId(int bookingId) throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String query = "SELECT DISTINCT p.* FROM passengers p " +
                      "INNER JOIN seats s ON p.passenger_id = s.passenger_id " +
                      "WHERE s.booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                passengers.add(extractPassengerFromResultSet(rs));
            }
        }
        return passengers;
    }

    public boolean create(Passenger passenger) throws SQLException {
        String query = "INSERT INTO passengers (full_name, date_of_birth, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, passenger.getFullName());
            stmt.setDate(2, Date.valueOf(passenger.getDateOfBirth()));
            stmt.setString(3, passenger.getEmail());
            stmt.setString(4, passenger.getPhone());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    passenger.setPassengerId(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Passenger passenger) throws SQLException {
        String query = "UPDATE passengers SET full_name = ?, date_of_birth = ?, email = ?, phone = ? WHERE passenger_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, passenger.getFullName());
            stmt.setDate(2, Date.valueOf(passenger.getDateOfBirth()));
            stmt.setString(3, passenger.getEmail());
            stmt.setString(4, passenger.getPhone());
            stmt.setInt(5, passenger.getPassengerId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int passengerId) throws SQLException {
        String query = "DELETE FROM passengers WHERE passenger_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, passengerId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Passenger extractPassengerFromResultSet(ResultSet rs) throws SQLException {
        Passenger passenger = new Passenger();
        passenger.setPassengerId(rs.getInt("passenger_id"));
        passenger.setFullName(rs.getString("full_name"));
        passenger.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        passenger.setEmail(rs.getString("email"));
        passenger.setPhone(rs.getString("phone"));
        passenger.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return passenger;
    }
}