package com.ecommerce.controller;


import com.ecommerce.repositories.UserRepository;
import com.ecommerce.security.request.LoginRequest;
import com.ecommerce.security.response.MassageResponse;
import com.ecommerce.security.response.RegisterResponse;
import com.ecommerce.security.response.UserInfoResponse;
import com.ecommerce.security.auth.AuthenticationService;
import com.ecommerce.security.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
           @RequestBody RegisterRequest request){
//
        if(userRepository.existsByUserName(request.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MassageResponse("Error: Username is already taken!"));
        }

        if(userRepository.existsByEmail(request.getEmail())){
            return ResponseEntity
                    .badRequest()
                    .body(new MassageResponse("Error: Email is already taken!"));
        }

        RegisterResponse registerResponse = authenticationService.register(request);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, registerResponse.getJwtCookie().toString())
                .body(registerResponse.getUserInfo());

    }

    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse>  authenticate(@RequestBody LoginRequest request){
        return authenticationService.authenticate(request);
    }
}
