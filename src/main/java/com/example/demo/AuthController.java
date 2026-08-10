package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ================= LOGIN PAGE =================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ================= REGISTER PAGE =================

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ================= REGISTER USER =================

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        try {

            authService.register(username, password);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Registration successful. Please login."
            );

            return "redirect:/login";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/register";
        }
    }
}