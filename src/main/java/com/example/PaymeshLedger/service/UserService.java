package com.example.PaymeshLedger.service;

import com.example.PaymeshLedger.entity.User;
import com.example.PaymeshLedger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User user){
        log.info("Creating user: {}", user.getEmail());

        User newUser = userRepository.save(user);

        log.info("User created with id: {} in database shardWallet{}", newUser.getId(), (newUser.getId() % 2 + 1));
        return newUser;
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}
