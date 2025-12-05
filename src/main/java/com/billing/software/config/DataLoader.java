package com.billing.software.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.billing.software.entity.Role;
import com.billing.software.entity.User;
import com.billing.software.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

   
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

  
    public DataLoader(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // First User is "ADMIN"
        if (!userRepository.existsByEmail("admin@billing.com")) {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail("admin@billing.com");
            adminUser.setPhone("9999999999");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            
            // Setting the Role from Enum
            adminUser.setRoles(Set.of(Role.ROLE_ADMIN)); 
            
            userRepository.save(adminUser);
            log.info("Default Admin User Inserted...");
        }

        log.info("Data Loading Complete...");
    }
}