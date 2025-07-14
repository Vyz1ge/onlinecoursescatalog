package com.example.onlinecoursescatalog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Course> courses;

    @Column(nullable = false, unique = true)
    private String username;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "user_favorite_courses",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> favoriteCourses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Rating> ratings = new HashSet<>();
}