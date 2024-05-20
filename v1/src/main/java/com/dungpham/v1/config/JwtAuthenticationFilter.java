package com.dungpham.v1.config;


import ch.qos.logback.core.boolex.EvaluationException;
import com.dungpham.v1.service.JWTService;
import com.dungpham.v1.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    private final UserService userService;

    private final List<Pair<String, String>> bypassPath = Arrays.asList(
            Pair.of("/api/v1/auth/signin", "POST"),
            Pair.of("/api/v1/auth/signup", "POST"),
            Pair.of("/v3/api-docs", "GET"),
            Pair.of("/swagger", "GET")
    );

    private boolean isBypassPath(HttpServletRequest request){
        return bypassPath.stream().anyMatch(pair -> request.getRequestURI().contains(pair.getFirst()) && pair.getSecond().equals(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if(isBypassPath(request)){
            filterChain.doFilter(request, response);
            return;
        }

        Cookie userCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("key"))
                .findFirst()
                .orElse(null);

        if(userCookie == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        jwt = userCookie.getValue();

//        if(!StringUtils.hasLength(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer ")){
//            filterChain.doFilter(request, response);
//            return;
//        }
//        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserName(jwt);

        if(StringUtils.hasLength(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

            if(jwtService.isTokenValid(jwt, userDetails)){
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request, response);
    }


}
