package com.airline.reservation.service;

import com.airline.reservation.dao.PassengerDAO;
import com.airline.reservation.model.Passenger;

import java.sql.SQLException;
import java.util.List;

public class PassengerService {
    private final PassengerDAO passengerDAO;

    public PassengerService() {
        this.passengerDAO = new PassengerDAO();
    }

    public Passenger getPassengerById(int passengerId) throws SQLException {
        return passengerDAO.findById(passengerId);
    }

    public List<Passenger> getAllPassengers() throws SQLException {
        return passengerDAO.findAll();
    }

    public List<Passenger> getPassengersByFlightId(int flightId) throws SQLException {
        return passengerDAO.findByFlightId(flightId);
    }

    public List<Passenger> getPassengersByBookingId(int bookingId) throws SQLException {
        return passengerDAO.findByBookingId(bookingId);
    }

    public boolean createPassenger(Passenger passenger) throws SQLException {
        return passengerDAO.create(passenger);
    }

    public boolean updatePassenger(Passenger passenger) throws SQLException {
        return passengerDAO.update(passenger);
    }

    public boolean deletePassenger(int passengerId) throws SQLException {
        return passengerDAO.delete(passengerId);
    }
}