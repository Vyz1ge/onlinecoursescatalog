package com.example.onlinecoursescatalog.service;

import com.example.onlinecoursescatalog.model.Course;
import com.example.onlinecoursescatalog.model.Rating;
import com.example.onlinecoursescatalog.model.User;
import com.example.onlinecoursescatalog.repository.CourseRepository;
import com.example.onlinecoursescatalog.repository.RatingRepository;
import com.example.onlinecoursescatalog.repository.TagRepository;
import com.example.onlinecoursescatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, TagRepository tagRepository, UserRepository userRepository, RatingRepository ratingRepository) {
        this.courseRepository = courseRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }


    public Double getAverageRatingForCourse(Long courseId) {
        Double averageRating = courseRepository.findAverageRatingByCourseId(courseId);
        return averageRating != null ? averageRating : 0.0;
    }



    public Optional<CourseWithAverageRating> getCourseWithAverageRating(Long id) {
        Optional<Course> courseOptional = courseRepository.findById(id);
        if (!courseOptional.isPresent()) {
            return Optional.empty();
        }
        Course course = courseOptional.get();
        Double averageRating = getAverageRatingForCourse(id);
        return Optional.of(new CourseWithAverageRating(course, averageRating));
    }


    public Optional<Rating> getUserRatingForCourse(Long userId, Long courseId) {
        return ratingRepository.findByUser_IdAndCourse_Id(userId, courseId);
    }

    public List<Course> getCoursesByTag(String tagName) {
        return courseRepository.findByTagName(tagName);
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public void deleteCourseById(Long id) {
        courseRepository.deleteById(id);
    }

    public void addCourseToFavorites(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        user.getFavoriteCourses().add(course);
        userRepository.save(user);
    }

    public void removeCourseFromFavorites(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        user.getFavoriteCourses().remove(course);
        userRepository.save(user);
    }

    public List<Course> getUserFavoriteCourses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (user.getFavoriteCourses().isEmpty()) {
            return List.of();
        }
        return courseRepository.findByIds(user.getFavoriteCourses().stream().map(Course::getId).collect(Collectors.toSet()));
    }

    public void addRatingToCourse(Long userId, Long courseId, int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        Optional<Rating> existingRating = ratingRepository.findByUser_IdAndCourse_Id(userId, courseId);

        if (existingRating.isPresent()) {
            throw new IllegalArgumentException("Вы уже оценили этот курс.");
        } else {
            Rating rating = new Rating();
            rating.setUser(user);
            rating.setCourse(course);
            rating.setScore(score);
            ratingRepository.save(rating);
        }
    }

    public static class CourseWithAverageRating {
        private final Course course;
        private final Double averageRating;

        public CourseWithAverageRating(Course course, Double averageRating) {
            this.course = course;
            this.averageRating = averageRating;
        }

        public Course getCourse() {
            return course;
        }

        public Double getAverageRating() {
            return averageRating;
        }
    }
}