package com.dungpham.v1.service.impl;


import com.dungpham.v1.dto.MessageResponse;
import com.dungpham.v1.dto.SignUpRequest;
import com.dungpham.v1.entity.Role;
import com.dungpham.v1.entity.User;
import com.dungpham.v1.repository.UserRepository;
import com.dungpham.v1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
            }
        };
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    @Override
    public Page<User> getUserByName(String name, Pageable pageable) {
        return userRepository.findByFirstName(name, pageable);
    }

    @Override
    public User updateUser(Integer id, User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User caller = (User) authentication.getPrincipal();
        var existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));

        switch (caller.getRole().name()) {
            case "EMPLOYEE":
                if (existingUser.getRole().equals(Role.CUSTOMER) || existingUser.getUserId() == caller.getUserId()) {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                    return userRepository.save(existingUser);
                } else {
                    throw new RuntimeException("You are not authorized to update this user");
                }

            case "ADMIN":
                existingUser.setFirstName(user.getFirstName());
                existingUser.setLastName(user.getLastName());
                existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                return userRepository.save(existingUser);

            case "CUSTOMER":
                if (existingUser.getUserId() == caller.getUserId()) {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                    return userRepository.save(existingUser);
                } else {
                    throw new RuntimeException("You are not authorized to update this user");
                }
default:
                throw new RuntimeException("You are not authorized to update this user");
        }
    }

    @Override
    public void deleteUser(Integer id) {
        boolean exists = userRepository.existsById(id);
        if (!exists) {
            throw new RuntimeException("User not found with id " + id);


        }
        userRepository.deleteById(id);
    }

    @Override
    public ResponseEntity addEmployee(SignUpRequest user) {
        // Kiểm tra xem email đã tồn tại trong hệ thống chưa
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            MessageResponse messageResponse = new MessageResponse("Email đã được sử dụng!");
            return ResponseEntity.status(400).body(messageResponse);
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setRole(Role.EMPLOYEE);
        newUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(newUser);
        return ResponseEntity.ok(user);
    }

    @Override
    public Page<User> getAllCustomer(Pageable pageable) {
        return userRepository.getAllCustomer(pageable);
    }

    @Override
    public User getCustomerById(Integer id) {
        return userRepository.findCustomerById(id).orElseThrow(() -> new RuntimeException("Customer not found with id " + id));

    }

    @Override
    public Page<User> findCustomerByFirstName(String name, Pageable pageable) {
        return userRepository.findCustomerByFirstName(name, pageable);
    }

}
