package com.airline.reservation.model;

public class Seat {
    private int seatId;
    private int flightId;
    private String seatNumber;
    private Integer bookingId;
    private Integer passengerId;
    private boolean isAvailable;

    public Seat() {}

    public Seat(int seatId, int flightId, String seatNumber, Integer bookingId, 
                Integer passengerId, boolean isAvailable) {
        this.seatId = seatId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.isAvailable = isAvailable;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Integer passengerId) {
        this.passengerId = passengerId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}