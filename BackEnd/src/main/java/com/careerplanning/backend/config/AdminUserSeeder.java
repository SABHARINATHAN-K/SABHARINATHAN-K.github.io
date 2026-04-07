package com.careerplanning.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.careerplanning.backend.modules.users.entity.CareerTrack;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;

@Configuration
public class AdminUserSeeder {

    @Bean
    public CommandLineRunner seedAdminUser(UserRepository userRepository) {
        return args -> {
            String adminEmail = "sabharinathan803@gmail.com";
            
            // Find existing user to update credentials, or create a new one
            User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElse(new User());
            
            admin.setFullName("Sabharinathan");
            admin.setEmail(adminEmail);
            admin.setPassword(new BCryptPasswordEncoder().encode("080306Ss@"));
            admin.setRole(UserRole.ADMIN.name());
            admin.setCareerTrack(CareerTrack.FULL_STACK_DEVELOPER.name());
            admin.setOnboardingCompleted(true);
            
            userRepository.save(admin);
            System.out.println("Admin user updated/seeded: " + adminEmail);
        };
    }
}
