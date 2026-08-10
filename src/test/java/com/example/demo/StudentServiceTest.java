package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {

        // Clean test database before each test
        studentRepository.deleteAll();

        // Add test student
        Student alice = new Student(
                "Alice",
                "Java",
                95
        );

        studentRepository.save(alice);
    }

    @Test
    void shouldFindStudentsByNameSearch() {

        var results =
                studentService.searchStudents("ali");

        assertThat(results)
                .isNotEmpty();

        assertThat(results.get(0).getName())
                .isEqualTo("Alice");
    }

    @Test
    void shouldReturnExcellentPerformanceForTopMarks() {

        Student student =
                new Student(
                        "Alice",
                        "Java",
                        95
                );

        assertThat(
                studentService.getPerformanceStatus(student)
        ).isEqualTo("Excellent");
    }
}