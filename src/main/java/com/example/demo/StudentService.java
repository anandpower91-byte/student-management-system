package com.example.demo;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // ==============================
    // GET ALL STUDENTS - PAGINATION
    // ==============================

    public Page<Student> getStudentsPage(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    // ==============================
    // GET ALL STUDENTS
    // ==============================

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ==============================
    // SEARCH STUDENTS
    // Searches ID, marks, name OR course
    // ==============================

    public List<Student> searchStudents(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll();
        }

        String search = keyword.trim();

        // ==============================
        // SEARCH BY ID
        // ==============================

        try {

            Long id = Long.parseLong(search);

            Student student =
                    studentRepository.findById(id).orElse(null);

            if (student != null) {
                return List.of(student);
            }

        } catch (NumberFormatException ignored) {
            // Keyword is not an ID
        }

        // ==============================
        // SEARCH BY MARKS
        // ==============================

        try {

            Integer marks = Integer.parseInt(search);

            List<Student> students =
                    studentRepository.findByMarks(marks);

            if (!students.isEmpty()) {
                return students;
            }

        } catch (NumberFormatException ignored) {
            // Keyword is not marks
        }

        // ==============================
        // SEARCH BY NAME OR COURSE
        // ==============================

        return studentRepository
                .findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase(
                        search,
                        search
                );
    }

    // ==============================
    // FIND STUDENT BY NAME
    // Used for Google Sheet sync
    // ==============================

    public Student findByName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        List<Student> students =
                studentRepository.findByNameIgnoreCase(name.trim());

        if (students.isEmpty()) {
            return null;
        }

        return students.get(0);
    }

    // ==============================
    // SAVE OR UPDATE STUDENT
    // ==============================

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    // ==============================
    // GET STUDENT BY ID
    // ==============================

    public Student getStudentById(Long id) {

        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found with id: " + id
                        )
                );
    }

    // ==============================
    // DELETE STUDENT
    // ==============================

    public void deleteStudent(Long id) {

        if (!studentRepository.existsById(id)) {

            throw new RuntimeException(
                    "Student not found with id: " + id
            );
        }

        studentRepository.deleteById(id);
    }

    // ==============================
    // DELETE ALL STUDENTS
    // ==============================

    public void deleteAllStudents() {
        studentRepository.deleteAll();
    }

    // ==============================
    // AVERAGE MARKS
    // ==============================

    public double getAverageMarks() {

        return studentRepository.findAll()
                .stream()
                .filter(student -> student.getMarks() != null)
                .mapToInt(Student::getMarks)
                .average()
                .orElse(0.0);
    }

    // ==============================
    // TOP PERFORMER
    // ==============================

    public Student getTopPerformer() {

        return studentRepository.findAll()
                .stream()
                .filter(student -> student.getMarks() != null)
                .max(Comparator.comparingInt(Student::getMarks))
                .orElse(null);
    }

    // ==============================
    // MOST POPULAR COURSE
    // ==============================

    public String getTopCourse() {

        return studentRepository.findAll()
                .stream()
                .filter(student -> student.getCourse() != null)
                .collect(Collectors.groupingBy(
                        Student::getCourse,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    // ==============================
    // PASS RATE
    // ==============================

    public double getPassRate() {

        List<Student> students =
                studentRepository.findAll();

        if (students.isEmpty()) {
            return 0.0;
        }

        long validStudents = students.stream()
                .filter(student -> student.getMarks() != null)
                .count();

        if (validStudents == 0) {
            return 0.0;
        }

        long passCount = students.stream()
                .filter(student ->
                        student.getMarks() != null &&
                        student.getMarks() >= 40
                )
                .count();

        return (passCount * 100.0) / validStudents;
    }

    // ==============================
    // PERFORMANCE STATUS
    // ==============================

    public String getPerformanceStatus(Student student) {

        if (student == null || student.getMarks() == null) {
            return "N/A";
        }

        int marks = student.getMarks();

        if (marks >= 90) {
            return "Excellent";

        } else if (marks >= 75) {
            return "Good";

        } else if (marks >= 60) {
            return "Average";

        } else if (marks >= 40) {
            return "Pass";

        } else {
            return "Needs Improvement";
        }
    }
}