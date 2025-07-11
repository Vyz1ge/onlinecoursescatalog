package com.example.onlinecoursescatalog.controller;

import com.example.onlinecoursescatalog.dto.CourseDTO;
import com.example.onlinecoursescatalog.model.Course;
import com.example.onlinecoursescatalog.model.Rating;
import com.example.onlinecoursescatalog.model.Tag;
import com.example.onlinecoursescatalog.model.User;
import com.example.onlinecoursescatalog.service.CourseService;
import com.example.onlinecoursescatalog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final TagService tagService;

    @Autowired
    public CourseController(CourseService courseService, TagService tagService) {
        this.courseService = courseService;
        this.tagService = tagService;
    }

    @GetMapping
    public String listCourses(Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }

        List<Course> courses = courseService.getAllCourses();
        List<Tag> tags = tagService.getAllTags();

        Map<Long, Rating> userRatingsMap = new HashMap<>();
        if (currentUser != null) {
            for (Course course : courses) {
                Optional<Rating> userRatingOptional = courseService.getUserRatingForCourse(currentUser.getId(), course.getId());
                userRatingOptional.ifPresent(rating -> userRatingsMap.put(course.getId(), rating));
            }
        }

        model.addAttribute("courses", courses);
        model.addAttribute("tags", tags);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRatingsMap", userRatingsMap);

        return "courses/list";
    }

    @GetMapping("/{id}")
    public String viewCourseDetails(@PathVariable Long id, Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }

        courseService.getCourseWithAverageRating(id)
                .ifPresentOrElse(
                        courseWithRating -> {
                            model.addAttribute("course", courseWithRating.getCourse());
                            model.addAttribute("averageRating", courseWithRating.getAverageRating());

                            Optional<Rating> userRatingOptional = courseService.getUserRatingForCourse(currentUser.getId(), id);
                            model.addAttribute("userRating", userRatingOptional.orElse(null));
                        },
                        () -> model.addAttribute("error", "Курс с ID " + id + " не найден.")
                );
        model.addAttribute("currentUser", currentUser);
        return "courses/details";
    }

    @GetMapping("/tag/{tagName}")
    public String listCoursesByTag(@PathVariable String tagName, Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }

        List<Course> courses = courseService.getCoursesByTag(tagName);
        List<Tag> allTags = tagService.getAllTags();

        Map<Long, Rating> userRatingsMap = new HashMap<>();
        if (currentUser != null) {
            for (Course course : courses) {
                Optional<Rating> userRatingOptional = courseService.getUserRatingForCourse(currentUser.getId(), course.getId());
                userRatingOptional.ifPresent(rating -> userRatingsMap.put(course.getId(), rating));
            }
        }

        model.addAttribute("courses", courses);
        model.addAttribute("selectedTag", tagName);
        model.addAttribute("tags", allTags);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRatingsMap", userRatingsMap);

        return "courses/list";
    }


    @GetMapping("/new")
    public String showNewCourseForm(Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }
        CourseDTO courseDTO = new CourseDTO();
        List<Tag> allTags = tagService.getAllTags();

        model.addAttribute("courseDTO", courseDTO);
        model.addAttribute("allTags", allTags);
        model.addAttribute("isNew", true);
        model.addAttribute("currentUser", currentUser);

        model.addAttribute("tagNameMap", allTags.stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName)));

        return "courses/add_edit";
    }

    @PostMapping("/new")
    public String createCourse(@Valid @ModelAttribute CourseDTO courseDTO, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }

        try {
            Course course = courseDTO.toCourse();
            Set<Tag> selectedTags = new HashSet<>();
            if (courseDTO.getTagIds() != null) {
                selectedTags = courseDTO.getTagIds().stream()
                        .map(tagId -> tagService.getTagById(tagId)
                                .orElseThrow(() -> new EntityNotFoundException("Тег не найден с ID: " + tagId)))
                        .collect(Collectors.toSet());
            }
            course.setTags(selectedTags);

            Course savedCourse = courseService.saveCourse(course);
            redirectAttributes.addFlashAttribute("success", "Курс '" + savedCourse.getTitle() + "' успешно создан!");
            return "redirect:/courses";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении курса: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось создать курс: " + e.getMessage());
        }
        return "redirect:/courses/new";
    }

    @GetMapping("/{id}/edit")
    public String showEditCourseForm(@PathVariable Long id, Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }

        return courseService.getCourseById(id)
                .map(course -> {
                    CourseDTO courseDTO = new CourseDTO(course);
                    List<Tag> allTags = tagService.getAllTags();

                    model.addAttribute("courseDTO", courseDTO);
                    model.addAttribute("allTags", allTags);
                    model.addAttribute("isNew", false);
                    model.addAttribute("currentUser", currentUser);
                    model.addAttribute("tagNameMap", allTags.stream()
                            .collect(Collectors.toMap(Tag::getId, Tag::getName)));
                    return "courses/add_edit";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Курс с ID " + id + " не найден.");
                    model.addAttribute("currentUser", currentUser);
                    return "courses/list";
                });
    }

    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id, @Valid @ModelAttribute CourseDTO courseDTO, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }

        try {
            Course existingCourse = courseService.getCourseById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Курс не найден для обновления: " + id));

            existingCourse.setTitle(courseDTO.getTitle());
            existingCourse.setSubject(courseDTO.getSubject());
            existingCourse.setDuration(courseDTO.getDuration());
            existingCourse.setPrice(courseDTO.getPrice());
            existingCourse.setDescription(courseDTO.getDescription());

            Set<Tag> selectedTags = new HashSet<>();
            if (courseDTO.getTagIds() != null) {
                selectedTags = courseDTO.getTagIds().stream()
                        .map(tagId -> tagService.getTagById(tagId)
                                .orElseThrow(() -> new EntityNotFoundException("Тег не найден с ID: " + tagId)))
                        .collect(Collectors.toSet());
            }
            existingCourse.setTags(selectedTags);

            Course updatedCourse = courseService.saveCourse(existingCourse);
            redirectAttributes.addFlashAttribute("success", "Курс '" + updatedCourse.getTitle() + "' успешно обновлен!");
            return "redirect:/courses";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении курса: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось обновить курс: " + e.getMessage());
        }
        return "redirect:/courses/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }

        try {
            String courseTitle = courseService.getCourseById(id).map(Course::getTitle).orElse("Неизвестный курс");
            courseService.deleteCourseById(id);
            redirectAttributes.addFlashAttribute("success", "Курс '" + courseTitle + "' успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить курс: " + e.getMessage());
        }
        return "redirect:/courses";
    }

    @PostMapping("/favorite/add/{courseId}")
    public String addToFavorites(@PathVariable Long courseId, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }
        try {
            courseService.addCourseToFavorites(currentUser.getId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Курс добавлен в избранное!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось добавить курс в избранное.");
        }
        return "redirect:/courses";
    }

    @PostMapping("/favorite/remove/{courseId}")
    public String removeFromFavorites(@PathVariable Long courseId, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }
        try {
            courseService.removeCourseFromFavorites(currentUser.getId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Курс удален из избранного.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить курс из избранного.");
        }
        return "redirect:/my-favorites";
    }

    @GetMapping("/my-favorites")
    public String listFavoriteCourses(Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }

        List<Course> favoriteCourses = courseService.getUserFavoriteCourses(currentUser.getId());

        model.addAttribute("favoriteCourses", favoriteCourses);
        model.addAttribute("currentUser", currentUser);
        return "courses/favorites";
    }

    @PostMapping("/{courseId}/rate")
    public String addRating(@PathVariable Long courseId, @RequestParam int score, @SessionAttribute(name = "currentUser", required = false) User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/";
        }
        try {
            courseService.addRatingToCourse(currentUser.getId(), courseId, score);
            redirectAttributes.addFlashAttribute("success", "Оценка успешно добавлена!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось добавить оценку.");
        }
        return "redirect:/courses/" + courseId;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, Model model, @SessionAttribute(name = "currentUser", required = false) User currentUser) {
        CourseDTO courseDTO = (CourseDTO) ex.getBindingResult().getTarget();

        List<Tag> allTags = tagService.getAllTags();
        model.addAttribute("allTags", allTags);
        model.addAttribute("tagNameMap", allTags.stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName)));

        model.addAttribute("courseDTO", courseDTO);
        model.addAttribute("isNew", courseDTO != null && courseDTO.getId() == null);
        model.addAttribute("currentUser", currentUser);

        return "courses/add_edit";
    }
}