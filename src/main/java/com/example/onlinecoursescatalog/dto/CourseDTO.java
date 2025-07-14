package com.example.onlinecoursescatalog.dto;

import com.example.onlinecoursescatalog.model.Course;
import com.example.onlinecoursescatalog.model.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class CourseDTO {
    private Long id;

    @NotEmpty(message = "Название курса не может быть пустым")
    private String title;

    @NotEmpty(message = "Тематика курса не может быть пустой")
    private String subject;

    private String duration;

    @NotNull(message = "Стоимость не может быть пустой")
    @DecimalMin(value = "0.00", message = "Стоимость не может быть отрицательной")
    @Digits(integer = 10, fraction = 2, message = "Некорректный формат стоимости (например, 99.99)")
    private BigDecimal price;

    private String description;
    private List<Long> tagIds;

    public CourseDTO(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.subject = course.getSubject();
        this.duration = course.getDuration();
        this.price = course.getPrice();
        this.description = course.getDescription();
        if (course.getTags() != null) {
            this.tagIds = course.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());
        } else {
            this.tagIds = java.util.Collections.emptyList();
        }
    }

    public Course toCourse() {
        Course course = new Course();
        course.setId(this.id);
        course.setTitle(this.title);
        course.setSubject(this.subject);
        course.setDuration(this.duration);
        course.setPrice(this.price);
        course.setDescription(this.description);
        return course;
    }
}