package com.airline.reservation.service;

import com.airline.reservation.dao.UserDAO;
import com.airline.reservation.model.User;

import java.sql.SQLException;

public class AuthenticationService {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) throws SQLException {
        currentUser = userDAO.authenticate(username, password);
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdministrator() {
        return currentUser != null && currentUser.getRole().name().equals("ADMINISTRATOR");
    }

    public boolean isStaff() {
        return currentUser != null && currentUser.getRole().name().equals("STAFF");
    }

    public boolean isPassenger() {
        return currentUser != null && currentUser.getRole().name().equals("PASSENGER");
    }
}