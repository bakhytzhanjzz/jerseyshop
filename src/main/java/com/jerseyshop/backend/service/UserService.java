package com.jerseyshop.backend.service;

import com.jerseyshop.backend.entity.Cart;
import com.jerseyshop.backend.entity.User;
import com.jerseyshop.backend.repository.CartRepository;
import com.jerseyshop.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            Hibernate.initialize(user.getOrders());
            Hibernate.initialize(user.getCart());
        });
        return users;
    }

    @Transactional
    public Optional<User> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> {
            Hibernate.initialize(u.getOrders());
            Hibernate.initialize(u.getCart());
        });
        return user;
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(User user) {
        logger.info("Creating user: {}", user.getUsername());
        validateUser(user);
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.error("Username already exists: {}", user.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("CUSTOMER");
        User savedUser = userRepository.save(user);
        logger.info("User saved with ID: {}", savedUser.getId());

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
        logger.info("Cart created for user ID: {}", savedUser.getId());

        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        validateUser(userDetails);
        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }
}