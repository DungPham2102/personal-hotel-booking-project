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

    // CÁC FUNCTION LIÊN QUAN TỚI USER

    // hiện ra tất cả customer hoặc theo tên
    @GetMapping("/customers")
    public ResponseEntity<Page<User>> getCustomerByName(@RequestParam(defaultValue = "") String name,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userService.findCustomerByFirstName(name, pageable);
        return ResponseEntity.ok(users);
    }

    // hiện ra customer theo id
    @GetMapping("/customers/{id}")
    public ResponseEntity<User> getCustomerById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getCustomerById(id));
    }

    // update user
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }



    // CÁC FUNCTION LIÊN QUAN TỚI ROOM




}
