package com.dungpham.v1.repository;


import com.dungpham.v1.entity.Role;
import com.dungpham.v1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

    Optional<User> findByEmail(String email);

    User findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name%")
    Page<User> findByFirstName(String name, Pageable pageable);

    @Query(value = "SELECT * FROM user WHERE role = 0", nativeQuery = true)
    Page<User> getAllCustomer(Pageable pageable);

    @Query(value = "SELECT * FROM user WHERE role = 0 AND user_id = %:id%", nativeQuery = true)
    Optional<User> findCustomerById(Integer id);

    @Query(value = "SELECT * FROM user WHERE first_name LIKE %:name% AND role = 0", nativeQuery = true)
    Page<User> findCustomerByFirstName(String name, Pageable pageable);
}
