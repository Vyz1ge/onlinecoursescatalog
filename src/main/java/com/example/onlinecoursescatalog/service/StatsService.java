package com.example.onlinecoursescatalog.service;

import com.example.onlinecoursescatalog.repository.CourseRepository;
import com.example.onlinecoursescatalog.repository.RatingRepository;
import com.example.onlinecoursescatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public StatsService(CourseRepository courseRepository, UserRepository userRepository, RatingRepository ratingRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    public long getCourseCount() {
        return courseRepository.count();
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public Map<Integer, Long> getRatingCounts() {
        List<Object[]> results = ratingRepository.countRatingsByScore();
        Map<Integer, Long> ratingCounts = new LinkedHashMap<>();
        for(int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }
        for (Object[] result : results) {
            Integer score = (Integer) result[0];
            Long count = (Long) result[1];
            if (score != null && count != null) {
                if(score >= 1 && score <= 5) {
                    ratingCounts.put(score, count);
                }
            }
        }
        return ratingCounts;
    }
}