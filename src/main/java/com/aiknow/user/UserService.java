package com.aiknow.user;

import com.aiknow.exception.DuplicateResourceException;
import com.aiknow.exception.ErrorCode;
import com.aiknow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String email, String password, String fullName, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User already exists with email: " + email, ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email, ErrorCode.USER_NOT_FOUND));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id, ErrorCode.USER_NOT_FOUND));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
