package com.airline.reservation.service;

import com.airline.reservation.dao.BookingDAO;
import com.airline.reservation.dao.FlightDAO;
import com.airline.reservation.dao.SeatDAO;
import com.airline.reservation.model.Booking;
import com.airline.reservation.model.Flight;
import com.airline.reservation.model.Seat;
import com.airline.reservation.model.enums.BookingStatus;
import com.airline.reservation.model.enums.FlightStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class BookingService {
    private final BookingDAO bookingDAO;
    private final FlightDAO flightDAO;
    private final SeatDAO seatDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.flightDAO = new FlightDAO();
        this.seatDAO = new SeatDAO();
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        return bookingDAO.findById(bookingId);
    }

    public Booking getBookingByReference(String bookingReference) throws SQLException {
        return bookingDAO.findByReference(bookingReference);
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.findAll();
    }

    public List<Booking> getBookingsByUserId(int userId) throws SQLException {
        return bookingDAO.findByUserId(userId);
    }

    public List<Booking> getBookingsByFlightId(int flightId) throws SQLException {
        return bookingDAO.findByFlightId(flightId);
    }

    public String createBooking(int flightId, int userId) throws SQLException {
        Flight flight = flightDAO.findById(flightId);
        
        if (flight == null) {
            throw new IllegalArgumentException("Flight not found");
        }

        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new IllegalStateException("Cannot book cancelled flight");
        }

        if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot book flight after departure");
        }

        List<Seat> availableSeats = seatDAO.findAvailableSeatsByFlightId(flightId);
        if (availableSeats.isEmpty()) {
            throw new IllegalStateException("No available seats");
        }

        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setFlightId(flightId);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.CONFIRMED);

        if (bookingDAO.create(booking)) {
            return booking.getBookingReference();
        }

        throw new SQLException("Failed to create booking");
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            return false;
        }

        seatDAO.releaseBookingSeats(bookingId);
        return bookingDAO.cancel(bookingId);
    }

    public boolean assignSeatToBooking(int bookingId, int seatId, int passengerId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Invalid or cancelled booking");
        }

        Seat seat = seatDAO.findById(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found");
        }

        if (seat.getFlightId() != booking.getFlightId()) {
            throw new IllegalArgumentException("Seat does not belong to this flight");
        }

        if (!seat.isAvailable()) {
            throw new IllegalStateException("Seat is not available");
        }

        return seatDAO.assignSeat(seatId, bookingId, passengerId);
    }

    public List<Seat> getBookingSeats(int bookingId) throws SQLException {
        return seatDAO.findByBookingId(bookingId);
    }

    private String generateBookingReference() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder reference = new StringBuilder();
        
        for (int i = 0; i < 6; i++) {
            reference.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return reference.toString();
    }
}