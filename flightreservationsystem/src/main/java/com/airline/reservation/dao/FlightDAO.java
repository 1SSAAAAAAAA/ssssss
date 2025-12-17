package com.airline.reservation.dao;

import com.airline.reservation.config.DatabaseConnection;
import com.airline.reservation.model.Flight;
import com.airline.reservation.model.enums.FlightStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {

    public Flight findById(int flightId) throws SQLException {
        String query = "SELECT * FROM flights WHERE flight_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractFlightFromResultSet(rs);
            }
        }
        return null;
    }

    public List<Flight> findAll() throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT * FROM flights ORDER BY departure_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                flights.add(extractFlightFromResultSet(rs));
            }
        }
        return flights;
    }

    public List<Flight> searchFlights(String origin, String destination, LocalDateTime departureDate) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM flights WHERE status = 'SCHEDULED'");
        
        if (origin != null && !origin.isEmpty()) {
            query.append(" AND origin LIKE ?");
        }
        if (destination != null && !destination.isEmpty()) {
            query.append(" AND destination LIKE ?");
        }
        if (departureDate != null) {
            query.append(" AND DATE(departure_time) = DATE(?)");
        }
        query.append(" ORDER BY departure_time");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;
            if (origin != null && !origin.isEmpty()) {
                stmt.setString(paramIndex++, "%" + origin + "%");
            }
            if (destination != null && !destination.isEmpty()) {
                stmt.setString(paramIndex++, "%" + destination + "%");
            }
            if (departureDate != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(departureDate));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                flights.add(extractFlightFromResultSet(rs));
            }
        }
        return flights;
    }

    public boolean create(Flight flight) throws SQLException {
        String query = "INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, total_seats, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, flight.getFlightNumber());
            stmt.setString(2, flight.getOrigin());
            stmt.setString(3, flight.getDestination());
            stmt.setTimestamp(4, Timestamp.valueOf(flight.getDepartureTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(flight.getArrivalTime()));
            stmt.setInt(6, flight.getTotalSeats());
            stmt.setString(7, flight.getStatus().name());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    flight.setFlightId(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Flight flight) throws SQLException {
        String query = "UPDATE flights SET flight_number = ?, origin = ?, destination = ?, departure_time = ?, arrival_time = ?, total_seats = ?, status = ? WHERE flight_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, flight.getFlightNumber());
            stmt.setString(2, flight.getOrigin());
            stmt.setString(3, flight.getDestination());
            stmt.setTimestamp(4, Timestamp.valueOf(flight.getDepartureTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(flight.getArrivalTime()));
            stmt.setInt(6, flight.getTotalSeats());
            stmt.setString(7, flight.getStatus().name());
            stmt.setInt(8, flight.getFlightId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int flightId) throws SQLException {
        String query = "DELETE FROM flights WHERE flight_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getAvailableSeatsCount(int flightId) throws SQLException {
        String query = "SELECT COUNT(*) FROM seats WHERE flight_id = ? AND is_available = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Flight extractFlightFromResultSet(ResultSet rs) throws SQLException {
        Flight flight = new Flight();
        flight.setFlightId(rs.getInt("flight_id"));
        flight.setFlightNumber(rs.getString("flight_number"));
        flight.setOrigin(rs.getString("origin"));
        flight.setDestination(rs.getString("destination"));
        flight.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
        flight.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
        flight.setTotalSeats(rs.getInt("total_seats"));
        flight.setStatus(FlightStatus.valueOf(rs.getString("status")));
        flight.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return flight;
    }
}