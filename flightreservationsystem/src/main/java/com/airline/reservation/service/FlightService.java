package com.airline.reservation.service;

import com.airline.reservation.dao.FlightDAO;
import com.airline.reservation.dao.SeatDAO;
import com.airline.reservation.model.Flight;
import com.airline.reservation.model.Seat;
import com.airline.reservation.model.enums.FlightStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class FlightService {
    private final FlightDAO flightDAO;
    private final SeatDAO seatDAO;

    public FlightService() {
        this.flightDAO = new FlightDAO();
        this.seatDAO = new SeatDAO();
    }

    public Flight getFlightById(int flightId) throws SQLException {
        return flightDAO.findById(flightId);
    }

    public List<Flight> getAllFlights() throws SQLException {
        return flightDAO.findAll();
    }

    public List<Flight> searchFlights(String origin, String destination, LocalDateTime departureDate) throws SQLException {
        return flightDAO.searchFlights(origin, destination, departureDate);
    }

    public boolean createFlight(Flight flight) throws SQLException {
        if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Departure time cannot be in the past");
        }
        
        if (flight.getArrivalTime().isBefore(flight.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }

        boolean created = flightDAO.create(flight);
        if (created) {
            generateSeats(flight.getFlightId(), flight.getTotalSeats());
        }
        return created;
    }

    public boolean updateFlight(Flight flight) throws SQLException {
        Flight existingFlight = flightDAO.findById(flight.getFlightId());
        if (existingFlight == null) {
            return false;
        }

        if (existingFlight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot modify flight after departure");
        }

        if (flight.getArrivalTime().isBefore(flight.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }

        if (flight.getTotalSeats() != existingFlight.getTotalSeats()) {
            seatDAO.deleteByFlightId(flight.getFlightId());
            generateSeats(flight.getFlightId(), flight.getTotalSeats());
        }

        return flightDAO.update(flight);
    }

    public boolean cancelFlight(int flightId) throws SQLException {
        Flight flight = flightDAO.findById(flightId);
        if (flight != null) {
            flight.setStatus(FlightStatus.CANCELLED);
            return flightDAO.update(flight);
        }
        return false;
    }

    public boolean deleteFlight(int flightId) throws SQLException {
        seatDAO.deleteByFlightId(flightId);
        return flightDAO.delete(flightId);
    }

    public int getAvailableSeatsCount(int flightId) throws SQLException {
        return flightDAO.getAvailableSeatsCount(flightId);
    }

    private void generateSeats(int flightId, int totalSeats) throws SQLException {
        int rows = (int) Math.ceil(totalSeats / 6.0);
        String[] columns = {"A", "B", "C", "D", "E", "F"};
        
        int seatCount = 0;
        for (int row = 1; row <= rows && seatCount < totalSeats; row++) {
            for (String col : columns) {
                if (seatCount >= totalSeats) break;
                Seat seat = new Seat();
                seat.setFlightId(flightId);
                seat.setSeatNumber(row + col);
                seat.setAvailable(true);
                seatDAO.create(seat);
                seatCount++;
            }
        }
    }
}