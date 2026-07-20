package com.shardedwalletsystem.demo.service;

import com.shardedwalletsystem.demo.model.User;
import com.shardedwalletsystem.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public User createUser(User user) {
        log.info("Creating user: {}", user.getEmail());
        User newUser = userRepository.save(user);
        log.info("Created user: {}", newUser.getEmail());
        return newUser;
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
    public List<User> getUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }
}
