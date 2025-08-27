package com.marketplace.service;

import com.marketplace.model.Role;
import com.marketplace.model.User;
import com.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setAddress("123 Test St");
        testUser.setPhoneNumber("555-1234");
        
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        testUser.setRoles(roles);
        
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void registerUser_withValidUser_shouldRegisterSuccessfully() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(result.getRoles().contains(Role.USER));
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_withExistingUsername_shouldThrowException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerUser(testUser));
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void registerUser_withExistingEmail_shouldThrowException() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerUser(testUser));
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void findByUsername_withValidUsername_shouldReturnUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByEmail_withValidEmail_shouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void getCurrentUser_withAuthenticatedUser_shouldReturnUser() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getCurrentUser_withAnonymousUser_shouldReturnNull() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When
        User result = userService.getCurrentUser();

        // Then
        assertNull(result);
    }

    @Test
    void getCurrentUser_withNoAuthentication_shouldReturnNull() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        User result = userService.getCurrentUser();

        // Then
        assertNull(result);
    }

    @Test
    void getCurrentUserId_withAuthenticatedUser_shouldReturnUserId() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        String result = userService.getCurrentUserId();

        // Then
        assertNotNull(result);
        assertEquals("user-123", result);
    }

    @Test
    void getCurrentUserId_withNoUser_shouldReturnNull() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        String result = userService.getCurrentUserId();

        // Then
        assertNull(result);
    }

    @Test
    void isUserLoggedIn_withAuthenticatedUser_shouldReturnTrue() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isUserLoggedIn();

        // Then
        assertTrue(result);
    }

    @Test
    void isUserLoggedIn_withNoUser_shouldReturnFalse() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean result = userService.isUserLoggedIn();

        // Then
        assertFalse(result);
    }

    @Test
    void updateUser_shouldSaveUser() {
        // Given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.updateUser(testUser);

        // Then
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateLastLoginTime_withValidUsername_shouldUpdateLoginTime() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateLastLoginTime("testuser");

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateLastLoginTime_withInvalidUsername_shouldNotUpdate() {
        // Given
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        // When
        userService.updateLastLoginTime("invalid");

        // Then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void promoteToAdmin_withValidUserId_shouldPromoteUser() {
        // Given
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.promoteToAdmin("user-123");

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void promoteToAdmin_withInvalidUserId_shouldThrowException() {
        // Given
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.promoteToAdmin("invalid"));
        assertEquals("User not found", exception.getMessage());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}