package com.dungpham.v1.controller;

import com.dungpham.v1.entity.User;
import com.dungpham.v1.repository.UserRepository;
import com.dungpham.v1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserService userService;

    // hiện ra tất cả customer
    @GetMapping("/customers")
    public ResponseEntity<Page<User>> getAllCustomer(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllCustomer(pageable);
        return ResponseEntity.ok(users);
    }

    // hiện ra customer theo id
    @GetMapping("/customers/{id}")
    public ResponseEntity<User> getCustomerById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getCustomerById(id));
    }

    @GetMapping(params = "name")
    public ResponseEntity<Page<User>> getUserByName(@RequestParam String name,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getUserByName(name, pageable);
        return ResponseEntity.ok(users);
    }

}
