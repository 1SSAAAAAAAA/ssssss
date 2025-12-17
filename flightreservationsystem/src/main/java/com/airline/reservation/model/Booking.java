package com.airline.reservation.model;

import com.airline.reservation.model.enums.BookingStatus;
import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private String bookingReference;
    private int flightId;
    private int userId;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking() {}

    public Booking(int bookingId, String bookingReference, int flightId, 
                   int userId, BookingStatus status, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.flightId = flightId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}