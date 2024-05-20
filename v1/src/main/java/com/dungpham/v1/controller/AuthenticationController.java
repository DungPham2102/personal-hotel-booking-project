package com.dungpham.v1.controller;


import com.dungpham.v1.dto.*;
import com.dungpham.v1.entity.User;
import com.dungpham.v1.repository.UserRepository;
import com.dungpham.v1.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    // đăng ký tài khoản Customer
    @Operation(summary = "Sign up a new customer account")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest){
        // Kiểm tra xem email đã tồn tại trong hệ thống chưa
        Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
        if (existingUser.isPresent()) {
            MessageResponse messageResponse = new MessageResponse("Email đã được sử dụng!");
            return ResponseEntity.status(400).body(messageResponse);
        }

        return ResponseEntity.ok(authenticationService.signup(signUpRequest));
    }

    // đăng nhập sử dụng email và password
    @PostMapping("/signin")
    @Operation(summary = "Sign in")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest signinRequest){
        var jwt = authenticationService.signin(signinRequest).getToken();

        ResponseCookie cookie = ResponseCookie.from("key", jwt)
                .maxAge(3600*24)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(authenticationService.signin(signinRequest));
    }


    // đăng xuất, cơ bản là việc xóa cookie 
    @PostMapping("/signout")
    @Operation(summary = "Sign out")
    public ResponseEntity<?> signout(HttpServletRequest request, HttpServletResponse response){
        Cookie userCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("key"))
                .findFirst()
                .orElse(null);

        if(userCookie == null){
            return ResponseEntity.badRequest().body(new MessageResponse("You are not signed in!"));
        }

        userCookie.setMaxAge(0);
        userCookie.setPath("/");
        response.addCookie(userCookie);

        return ResponseEntity.ok(new MessageResponse("Sign out successfully!"));
    }


//    @PostMapping("/refresh")
//    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest){
//        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
//    }

}
