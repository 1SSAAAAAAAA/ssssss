package com.airline.reservation.dao;

import com.airline.reservation.config.DatabaseConnection;
import com.airline.reservation.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    public Seat findById(int seatId) throws SQLException {
        String query = "SELECT * FROM seats WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, seatId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractSeatFromResultSet(rs);
            }
        }
        return null;
    }

    public List<Seat> findByFlightId(int flightId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT * FROM seats WHERE flight_id = ? ORDER BY seat_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seats.add(extractSeatFromResultSet(rs));
            }
        }
        return seats;
    }

    public List<Seat> findAvailableSeatsByFlightId(int flightId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT * FROM seats WHERE flight_id = ? AND is_available = TRUE ORDER BY seat_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seats.add(extractSeatFromResultSet(rs));
            }
        }
        return seats;
    }

    public List<Seat> findByBookingId(int bookingId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT * FROM seats WHERE booking_id = ? ORDER BY seat_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seats.add(extractSeatFromResultSet(rs));
            }
        }
        return seats;
    }

    public boolean create(Seat seat) throws SQLException {
        String query = "INSERT INTO seats (flight_id, seat_number, booking_id, passenger_id, is_available) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, seat.getFlightId());
            stmt.setString(2, seat.getSeatNumber());
            if (seat.getBookingId() != null) {
                stmt.setInt(3, seat.getBookingId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            if (seat.getPassengerId() != null) {
                stmt.setInt(4, seat.getPassengerId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setBoolean(5, seat.isAvailable());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    seat.setSeatId(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean assignSeat(int seatId, int bookingId, int passengerId) throws SQLException {
        String query = "UPDATE seats SET booking_id = ?, passenger_id = ?, is_available = FALSE WHERE seat_id = ? AND is_available = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            stmt.setInt(2, passengerId);
            stmt.setInt(3, seatId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean releaseSeat(int seatId) throws SQLException {
        String query = "UPDATE seats SET booking_id = NULL, passenger_id = NULL, is_available = TRUE WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, seatId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean releaseBookingSeats(int bookingId) throws SQLException {
        String query = "UPDATE seats SET booking_id = NULL, passenger_id = NULL, is_available = TRUE WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int seatId) throws SQLException {
        String query = "DELETE FROM seats WHERE seat_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, seatId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteByFlightId(int flightId) throws SQLException {
        String query = "DELETE FROM seats WHERE flight_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Seat extractSeatFromResultSet(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setSeatId(rs.getInt("seat_id"));
        seat.setFlightId(rs.getInt("flight_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        
        int bookingId = rs.getInt("booking_id");
        seat.setBookingId(rs.wasNull() ? null : bookingId);
        
        int passengerId = rs.getInt("passenger_id");
        seat.setPassengerId(rs.wasNull() ? null : passengerId);
        
        seat.setAvailable(rs.getBoolean("is_available"));
        return seat;
    }
}