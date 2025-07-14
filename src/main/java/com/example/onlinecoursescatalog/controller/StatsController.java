package com.example.onlinecoursescatalog.controller;

import com.example.onlinecoursescatalog.model.Role;
import com.example.onlinecoursescatalog.model.User;
import com.example.onlinecoursescatalog.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public String showStats(Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null || currentUser.getRole() == Role.USER) {
            return "redirect:/";
        }
        long courseCount = statsService.getCourseCount();
        long userCount = statsService.getUserCount();
        Map<Integer, Long> ratingCounts = statsService.getRatingCounts();

        List<Integer> ratingLabels = new ArrayList<>(ratingCounts.keySet());
        List<Long> ratingData = new ArrayList<>(ratingCounts.values());

        model.addAttribute("courseCount", courseCount);
        model.addAttribute("userCount", userCount);
        model.addAttribute("ratingLabels", ratingLabels);
        model.addAttribute("ratingData", ratingData);
        model.addAttribute("currentUser", currentUser);

        return "stats/dashboard";
    }
}