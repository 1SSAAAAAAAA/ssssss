package com.airline.reservation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Passenger {
    private int passengerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public Passenger() {}

    public Passenger(int passengerId, String fullName, LocalDate dateOfBirth, 
                    String email, String phone, LocalDateTime createdAt) {
        this.passengerId = passengerId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}