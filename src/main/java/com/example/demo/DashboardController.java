package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StudentService studentService;

    public DashboardController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ==============================
    // DASHBOARD
    // ==============================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Get all students
        List<Student> students =
                studentService.getAllStudents();

        // ==============================
        // BASIC STATISTICS
        // ==============================

        int totalStudents =
                students.size();

        double averageMarks =
                studentService.getAverageMarks();

        double passRate =
                studentService.getPassRate();

        Student topPerformer =
                studentService.getTopPerformer();

        String topCourse =
                studentService.getTopCourse();

        // ==============================
        // COURSE COUNTS
        // ==============================

        Map<String, Long> courseCounts =
                students.stream()
                        .filter(student ->
                                student.getCourse() != null &&
                                !student.getCourse().isBlank())
                        .collect(Collectors.groupingBy(
                                Student::getCourse,
                                Collectors.counting()
                        ));

        // ==============================
        // STUDENT NAMES FOR CHART
        // ==============================

        List<String> studentNames =
                students.stream()
                        .map(Student::getName)
                        .toList();

        // ==============================
        // STUDENT MARKS FOR CHART
        // ==============================

        List<Integer> studentMarks =
                students.stream()
                        .map(Student::getMarks)
                        .toList();

        // ==============================
        // SEND DATA TO THYMELEAF
        // ==============================

        model.addAttribute(
                "students",
                students
        );

        model.addAttribute(
                "totalStudents",
                totalStudents
        );

        model.addAttribute(
                "averageMarks",
                averageMarks
        );

        model.addAttribute(
                "passRate",
                passRate
        );

        model.addAttribute(
                "topPerformer",
                topPerformer
        );

        model.addAttribute(
                "topCourse",
                topCourse
        );

        model.addAttribute(
                "courseCounts",
                courseCounts
        );

        model.addAttribute(
                "studentNames",
                studentNames
        );

        model.addAttribute(
                "studentMarks",
                studentMarks
        );

        return "dashboard";
    }
}