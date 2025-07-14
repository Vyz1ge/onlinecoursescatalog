package com.example.onlinecoursescatalog.service;

import com.example.onlinecoursescatalog.model.Role;
import com.example.onlinecoursescatalog.model.User;
import com.example.onlinecoursescatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findOrCreateUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setRole(Role.USER);
            return userRepository.save(newUser);
        });
    }

    public long getUserCount() {
        return userRepository.count();
    }
}