package com.hotelmanagement.system.service;

import com.hotelmanagement.system.model.User;
import com.hotelmanagement.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        // Check if email already exists in DATABASE (dynamic)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists.");
        }

        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("GUEST");
        }

        // Save user to DATABASE (dynamic)
        User savedUser = userRepository.save(user);

        // Create default wallet card (dynamic)
        walletService.createDefaultCardForUser(savedUser);

        return savedUser;
    }

    @Override
    public User loginUser(String email, String password) {
        // Find user from DATABASE (dynamic)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        // Check password (dynamic)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password.");
        }

        return user;
    }

    @Override
    public User getUserById(Long userId) {
        // Fetch from DATABASE (dynamic)
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    @Override
    public List<User> getAllUsers() {
        // Fetch all from DATABASE (dynamic)
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        User existingUser = getUserById(userId);
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setRole(userDetails.getRole());
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
}