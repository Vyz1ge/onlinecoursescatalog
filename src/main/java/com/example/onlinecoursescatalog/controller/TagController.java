package com.example.onlinecoursescatalog.controller;

import com.example.onlinecoursescatalog.model.Tag;
import com.example.onlinecoursescatalog.model.User;
import com.example.onlinecoursescatalog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/new")
    public String showNewTagForm(Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }
        model.addAttribute("tag", new Tag());
        model.addAttribute("currentUser", currentUser);
        return "tags/add_edit";
    }

    @PostMapping("/save")
    public String saveTag(@ModelAttribute Tag tag, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }

        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Имя тега не может быть пустым.");
            return "redirect:/tags/new";
        }

        Optional<Tag> existingTag = tagService.getTagByName(tag.getName().trim());
        if (existingTag.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Тег '" + tag.getName().trim() + "' уже существует.");
            return "redirect:/tags/new";
        }

        try {
            Tag savedTag = tagService.saveTag(tag);
            redirectAttributes.addFlashAttribute("success", "Тег '" + savedTag.getName() + "' успешно добавлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось добавить тег: " + e.getMessage());
        }
        return "redirect:/courses";
    }
}