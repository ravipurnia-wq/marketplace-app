package com.marketplace.config;

import com.marketplace.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Gateway endpoints
                .requestMatchers("/gateway/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Public pages and resources
                .requestMatchers("/", "/products", "/about", "/contact", "/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                
                // API endpoints (both direct and through gateway)
                .requestMatchers("/api/products/**", "/gateway/api/products/**").permitAll()
                .requestMatchers("/api/payments/**", "/gateway/api/payments/**").hasRole("USER")
                .requestMatchers("/api/cart/**", "/gateway/api/cart/**").hasRole("USER")
                .requestMatchers("/api/orders/**", "/gateway/api/orders/**").hasRole("USER")
                
                // Admin endpoints (both direct and through gateway)
                .requestMatchers("/admin/**", "/gateway/admin/**").hasRole("ADMIN")
                
                // User-specific pages
                .requestMatchers("/cart/**", "/orders/**", "/profile/**").hasRole("USER")
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/gateway/**", "/actuator/**")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );
        
        return http.build();
    }
}