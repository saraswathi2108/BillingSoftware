package com.billing.software.controller;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.software.dto.ChangePasswordRequest;
import com.billing.software.dto.ForgotPasswordRequest;
import com.billing.software.dto.LoginRequest;
import com.billing.software.dto.RegisterRequest;
import com.billing.software.dto.ResetPasswordWithOtpRequest;
import com.billing.software.entity.Role;
import com.billing.software.entity.User;
import com.billing.software.repository.UserRepository;
import com.billing.software.security.JwtService;
import com.billing.software.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthService authService;
    

    //registration 
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        
        user.getRoles().add(Role.ROLE_CASHIER);

        userRepository.save(user);

        return ResponseEntity.ok("User Registered Successfully");
    }

    // ✅ USER LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            var authorities = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            // 2. Create a claims map to hold the roles
            Map<String, Object> extraClaims = Map.of("roles", authorities);


            log.info("Extra claims {} ", extraClaims);
            // 3. Call createToken (instead of generateToken) to include the claims
            String token = jwtService.createToken(extraClaims, loginRequest.getEmail()); 
            
            
            log.info("User logged in Succesfully: {}",loginRequest.getEmail());
            
            log.info("Sending JWT token to app {}:",token);
            
            User user = userRepository.findByEmail(loginRequest.getEmail())
            		.orElseThrow(() -> new RuntimeException("User not found with email "+ loginRequest.getEmail()));
            
            
            return ResponseEntity.ok(token);


        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid Credentials");
        }
    }

    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal principal) {
    	
    	String email = principal.getName();
    	log.info("Username: {} ", email);
    	return authService.changePassword(changePasswordRequest, email);
    }
    
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
    	
        return ResponseEntity.ok(Map.of(
                "message", authService.sendOtp(req)
        ));

    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordWithOtpRequest req) {
        return ResponseEntity.ok(Map.of(
                "message", authService.resetPassword(req)
        ));
    }

}