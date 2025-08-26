package com.marketplace.config;

import com.marketplace.model.Role;
import com.marketplace.model.User;
import com.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            createAdminUser();
            createTestUser();
        }
    }
    
    private void createAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@techmarketpro.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setAddress("123 Admin Street, Tech City, TC 12345");
        admin.setPhoneNumber("+1 (555) 123-4567");
        admin.getRoles().add(Role.ADMIN);
        admin.getRoles().add(Role.USER);
        
        userRepository.save(admin);
        System.out.println("✅ Created admin user: admin/admin123");
    }
    
    private void createTestUser() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setAddress("456 Test Lane, Demo City, DC 67890");
        testUser.setPhoneNumber("+1 (555) 987-6543");
        testUser.getRoles().add(Role.USER);
        
        userRepository.save(testUser);
        System.out.println("✅ Created test user: testuser/password");
    }
}