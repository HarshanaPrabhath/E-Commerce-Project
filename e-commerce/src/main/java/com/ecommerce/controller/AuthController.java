package com.ecommerce.controller;


import com.ecommerce.repositories.UserRepository;
import com.ecommerce.security.request.LoginRequest;
import com.ecommerce.security.response.MassageResponse;
import com.ecommerce.security.response.UserInfoResponse;
import com.ecommerce.security.auth.AuthenticationService;
import com.ecommerce.security.request.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @Valid @RequestBody RegisterRequest request){

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
        return ResponseEntity.ok(authenticationService.register(request) + " User Registered Successfully!");

    }

    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse>  authenticate(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
