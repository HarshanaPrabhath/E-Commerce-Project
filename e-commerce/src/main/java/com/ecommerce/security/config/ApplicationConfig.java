package com.ecommerce.security.config;


import com.ecommerce.model.AppRole;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repositories.RoleRepository;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

//    private final UserRepository userRepository;

//    @Bean
//    public UserDetailsService userDetailsService(UserRepository userRepository) {
//        return username -> userRepository.findByUserName(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
//                .requestMatchers("/api/admin/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
//                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
                .headers(headers ->
                        headers.frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers(
                "/v2/api-docs",
                "/swagger-resource",
                "/configuration/security",
                "/swagger-ui/",
                "webjars/**"
        ));
    }
//    @Bean
//    public CommandLineRunner initUsers(UserRepository userRepository,
//                                       RoleRepository roleRepository,
//                                       PasswordEncoder passwordEncoder) {
//        return args -> {
//            userRepository.deleteAll(); // optional
//
//            // Ensure roles are saved first (if not already there)
//            roleRepository.save(new Role(AppRole.ROLE_USER));
//            roleRepository.save(new Role(AppRole.ROLE_ADMIN));
//            roleRepository.save(new Role(AppRole.ROLE_SELLER));
//
//            // ✅ Fetch managed role entities from DB
//            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow();
//            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow();
//
//            // Create user1
//            User user1 = new User();
//            user1.setUserName("john");
//            user1.setEmail("john@example.com");
//            user1.setPassword(passwordEncoder.encode("userpass"));
//            user1.setRoles(new HashSet<>(List.of(userRole)));
//
//            // Create user2
//            User user2 = new User();
//            user2.setUserName("admin");
//            user2.setEmail("admin@example.com");
//            user2.setPassword(passwordEncoder.encode("adminpass"));
//            user2.setRoles(new HashSet<>(List.of(adminRole)));
//
//            userRepository.saveAll(List.of(user1, user2));
//
//            System.out.println("✅ Sample users initialized in H2 database.");
//        };
//    }

}

