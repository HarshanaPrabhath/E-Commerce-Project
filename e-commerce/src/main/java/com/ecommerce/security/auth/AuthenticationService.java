package com.ecommerce.security.auth;

import com.ecommerce.model.AppRole;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repositories.RoleRepository;
import com.ecommerce.repositories.UserRepository;
import com.ecommerce.security.config.JwtService;

import com.ecommerce.security.request.LoginRequest;
import com.ecommerce.security.request.RegisterRequest;
import com.ecommerce.security.response.UserInfoResponse;
import com.ecommerce.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
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

    public UserInfoResponse register(RegisterRequest request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Set<String> strRoles = request.getRole();
        Set<Role> roles = new HashSet<>();

        if(strRoles != null){
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
        }else{
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
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        roles.add(userRole);
                }
            });
        }

        User user = new User();
        user.setUserName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRoles(roles);


        userRepository.save(user);

        String jwtToken = jwtService.generateToken(UserDetailsImpl.build(user));

        return UserInfoResponse.builder()
                .jwtToken(jwtToken)
                .build();
    }

   public UserInfoResponse authenticate(LoginRequest request) {

      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
              request.getEmail(),
              request.getPassword()

      ));
      var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
      var jwtToken = jwtService.generateToken(UserDetailsImpl.build(user));

      UserInfoResponse  userInfoResponse =  new UserInfoResponse();
       userInfoResponse.setJwtToken(jwtToken);
       userInfoResponse.setUserId(user.getUserId());
       userInfoResponse.setEmail(user.getEmail());
       userInfoResponse.setUsername(user.getUserName());
       userInfoResponse.setRoles(
               user.getRoles().stream()
                       .map(role -> role.getRoleName().name()) // assuming getRoleName() returns AppRole enum
                       .collect(Collectors.toList())
       );

      return  userInfoResponse;
   }
}

