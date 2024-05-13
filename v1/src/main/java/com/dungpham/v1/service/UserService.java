package com.dungpham.v1.service;

import com.dungpham.v1.dto.SignUpRequest;
import com.dungpham.v1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

    UserDetailsService userDetailsService();

    Page<User> getAllUsers(Pageable pageable);

    User getUserById(Integer id);

    Page<User> getUserByName(String name, Pageable pageable);

    User updateUser(Integer id, User user);

    void deleteUser(Integer id);

    ResponseEntity addEmployee(SignUpRequest user);

    Page<User> getAllCustomer(Pageable pageable);

    User getCustomerById(Integer id);


    Page<User> findCustomerByFirstName(String name, Pageable pageable);

//    User getCustomerInfo();
}
