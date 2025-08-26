package com.marketplace.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        String errorCode = "Unknown";
        String errorMessage = "An unexpected error occurred";
        String requestedUrl = "";
        
        if (status != null) {
            errorCode = status.toString();
            
            switch (errorCode) {
                case "404":
                    errorMessage = "Page not found";
                    break;
                case "405":
                    errorMessage = "Method not allowed";
                    break;
                case "500":
                    errorMessage = "Internal server error";
                    break;
                default:
                    errorMessage = "Error occurred";
            }
        }
        
        if (uri != null) {
            requestedUrl = uri.toString();
        }
        
        if (message != null) {
            errorMessage += " - " + message.toString();
        }
        
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("requestedUrl", requestedUrl);
        
        return "error";
    }
}