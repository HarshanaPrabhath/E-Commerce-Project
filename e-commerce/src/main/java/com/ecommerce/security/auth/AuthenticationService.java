package com.ecommerce.security.auth;

import com.ecommerce.model.AppRole;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repositories.RoleRepository;
import com.ecommerce.repositories.UserRepository;
import com.ecommerce.security.config.JwtService;

import com.ecommerce.security.request.LoginRequest;
import com.ecommerce.security.request.RegisterRequest;
import com.ecommerce.security.response.RegisterResponse;
import com.ecommerce.security.response.UserInfoResponse;
import com.ecommerce.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public RegisterResponse register(RegisterRequest request) {
        // Encode the password before saving it
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Initialize a set of roles
        Set<String> strRoles = request.getRole(); // Can be null or have roles like ["admin"]
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // Default role (ROLE_USER) when no role is provided
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
        } else {
            // Loop through the provided roles (e.g. ["admin", "seller"])
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        roles.add(sellerRole);
                        break;
                    default:
                        // If role doesn't match known ones, default to user role
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        roles.add(userRole);
                        break;
                }
            });
        }

        // Create the user with the roles
        User user = new User();
        user.setUserName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRoles(roles);

        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Generate JWT Token
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(UserDetailsImpl.build(user));
        // Return response with JWT token

        UserInfoResponse userInfoResponse = UserInfoResponse
                .builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUserName())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles()
                        .stream()
                        .map(role -> role.getRoleName().name()) // assuming getRoleName() returns AppRole enum
                        .collect(Collectors.toList()))
                .build();



        return new RegisterResponse(userInfoResponse, jwtCookie);
    }

    public ResponseEntity<UserInfoResponse> authenticate(LoginRequest request) {

      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
              request.getEmail(),
              request.getPassword()

      ));
      var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(UserDetailsImpl.build(user));

      UserInfoResponse  userInfoResponse =  new UserInfoResponse();
//       userInfoResponse.setJwtToken(jwtCookie.toString());
       userInfoResponse.setUserId(user.getUserId());
       userInfoResponse.setEmail(user.getEmail());
       userInfoResponse.setUsername(user.getUserName());
       userInfoResponse.setRoles(
               user.getRoles().stream()
                       .map(role -> role.getRoleName().name()) // assuming getRoleName() returns AppRole enum
                       .collect(Collectors.toList())
       );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(userInfoResponse);
   }
}

