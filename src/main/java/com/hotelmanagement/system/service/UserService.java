package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.User;
import java.util.List;

// This is the INTERFACE. It only declares the methods.
public interface UserService {
    User registerUser(User user);
    User loginUser(String email, String password);
    User getUserById(Long userId);
    List<User> getAllUsers();
    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
}