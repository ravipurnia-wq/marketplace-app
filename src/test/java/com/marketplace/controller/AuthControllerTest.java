package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");
        testUser.setPassword("password123");
    }

    @Test
    void login_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_withError_shouldAddErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid username or password"));
    }

    @Test
    void register_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerUser_withValidUser_shouldRedirectToLogin() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("firstName", "Test")
                .param("lastName", "User")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("success", "Registration successful! Please log in."));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    void registerUser_withServiceException_shouldReturnRegisterViewWithError() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("firstName", "Test")
                .param("lastName", "User")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username already exists"));
    }

    @Test
    @WithMockUser
    void profile_withAuthenticatedUser_shouldReturnProfileView() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", testUser));

        verify(userService).getCurrentUser();
    }

    @Test
    void profile_withoutAuthenticatedUser_shouldRedirectToLogin() throws Exception {
        when(userService.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).getCurrentUser();
    }

    @Test
    @WithMockUser
    void updateProfile_withValidUser_shouldUpdateAndReturnProfile() throws Exception {
        User updatedUser = new User();
        updatedUser.setId("1");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated@example.com");
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(post("/profile")
                .param("firstName", "Updated")
                .param("lastName", "User")
                .param("email", "updated@example.com")
                .param("phoneNumber", "9876543210")
                .param("address", "456 Updated St")
                .param("username", "testuser")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getCurrentUser();
        verify(userService).updateUser(any(User.class));
    }

    @Test
    @WithMockUser
    void updateProfile_withoutAuthenticatedUser_shouldReturnProfileView() throws Exception {
        when(userService.getCurrentUser()).thenReturn(null);

        mockMvc.perform(post("/profile")
                .param("firstName", "Updated")
                .param("lastName", "User")
                .param("email", "updated@example.com")
                .param("username", "testuser")
                .param("password", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).getCurrentUser();
        verify(userService, never()).updateUser(any(User.class));
    }
}