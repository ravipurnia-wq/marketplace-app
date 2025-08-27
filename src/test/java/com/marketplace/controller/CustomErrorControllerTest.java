package com.marketplace.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.RequestDispatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomErrorController.class)
@ActiveProfiles("test")
class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleError_with404Status_shouldReturn404ErrorPage() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/nonexistent")
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Not Found"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "404"))
                .andExpect(model().attribute("errorMessage", "Page not found - Not Found"))
                .andExpect(model().attribute("requestedUrl", "/nonexistent"));
    }

    @Test
    void handleError_with405Status_shouldReturn405ErrorPage() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 405)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/products")
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Method Not Allowed"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "405"))
                .andExpect(model().attribute("errorMessage", "Method not allowed - Method Not Allowed"))
                .andExpect(model().attribute("requestedUrl", "/api/products"));
    }

    @Test
    void handleError_with500Status_shouldReturn500ErrorPage() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/products")
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Internal Server Error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "500"))
                .andExpect(model().attribute("errorMessage", "Internal server error - Internal Server Error"))
                .andExpect(model().attribute("requestedUrl", "/api/products"));
    }

    @Test
    void handleError_withUnknownStatus_shouldReturnGenericErrorPage() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 418)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/teapot")
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "I'm a teapot"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "418"))
                .andExpect(model().attribute("errorMessage", "Error occurred - I'm a teapot"))
                .andExpect(model().attribute("requestedUrl", "/teapot"));
    }

    @Test
    void handleError_withNoAttributes_shouldReturnDefaultErrorPage() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "Unknown"))
                .andExpect(model().attribute("errorMessage", "An unexpected error occurred"))
                .andExpect(model().attribute("requestedUrl", ""));
    }

    @Test
    void handleError_withOnlyStatusCode_shouldReturnBasicErrorPage() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "404"))
                .andExpect(model().attribute("errorMessage", "Page not found"))
                .andExpect(model().attribute("requestedUrl", ""));
    }

    @Test
    void handleError_withNullMessage_shouldNotAppendMessage() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/test"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorCode", "404"))
                .andExpect(model().attribute("errorMessage", "Page not found"))
                .andExpect(model().attribute("requestedUrl", "/test"));
    }
}