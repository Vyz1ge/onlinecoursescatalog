package com.example.onlinecoursescatalog.repository;

import com.example.onlinecoursescatalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c JOIN c.tags t WHERE t.name = :tagName")
    List<Course> findByTagName(String tagName);

    @Query("SELECT c FROM Course c WHERE c.id IN :courseIds")
    List<Course> findByIds(Set<Long> courseIds);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(Long courseId);
}