package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Search by student name
    List<Student> findByNameContainingIgnoreCase(String name);

    // Search by student name OR course
    List<Student> findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase(
            String name,
            String course
    );

    // Find exact student name, ignoring uppercase/lowercase
    List<Student> findByNameIgnoreCase(String name);

    // Search by exact marks
    List<Student> findByMarks(Integer marks);
}