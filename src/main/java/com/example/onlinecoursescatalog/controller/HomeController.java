package com.example.onlinecoursescatalog.controller;

import com.example.onlinecoursescatalog.model.User;
import com.example.onlinecoursescatalog.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class HomeController {

    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/courses";
        }
        return "users/login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, HttpSession session, RedirectAttributes redirectAttributes) {
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username cannot be empty.");
            return "redirect:/";
        }
        User user = userService.findOrCreateUserByUsername(username.trim());
        session.setAttribute("currentUser", user);
        return "redirect:/courses";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}