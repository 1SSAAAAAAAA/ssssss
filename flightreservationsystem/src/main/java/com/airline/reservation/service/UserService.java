package com.airline.reservation.service;

import com.airline.reservation.dao.UserDAO;
import com.airline.reservation.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public User getUserById(int userId) throws SQLException {
        return userDAO.findById(userId);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public boolean createUser(User user) throws SQLException {
        return userDAO.create(user);
    }

    public boolean updateUser(User user) throws SQLException {
        return userDAO.update(user);
    }

    public boolean deleteUser(int userId) throws SQLException {
        return userDAO.delete(userId);
    }

    public boolean isUsernameAvailable(String username) throws SQLException {
        List<User> users = userDAO.findAll();
        return users.stream().noneMatch(u -> u.getUsername().equals(username));
    }
}