package com.example.onlinecoursescatalog.repository;

import com.example.onlinecoursescatalog.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUser_IdAndCourse_Id(Long userId, Long courseId);


    @Query("SELECT r.score, COUNT(r) FROM Rating r GROUP BY r.score ORDER BY r.score")
    List<Object[]> countRatingsByScore();
}