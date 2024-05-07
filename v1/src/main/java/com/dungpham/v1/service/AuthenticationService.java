package com.dungpham.v1.service;

import com.dungpham.v1.dto.JwtAuthenticationResponse;
import com.dungpham.v1.dto.RefreshTokenRequest;
import com.dungpham.v1.dto.SignUpRequest;
import com.dungpham.v1.dto.SigninRequest;
import com.dungpham.v1.entity.User;

public interface AuthenticationService {

    User signup(SignUpRequest signUpRequest);

    JwtAuthenticationResponse signin(SigninRequest signinRequest);

    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

}
