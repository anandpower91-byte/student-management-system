package com.example.demo;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StudentDTO;
import com.example.demo.dto.StudentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
@Tag(
        name = "Student Management",
        description = "APIs for managing students"
)
public class StudentRestController {

    private final StudentService studentService;

    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ==============================
    // GET ALL STUDENTS
    // ==============================

    @Operation(
            summary = "Get all students",
            description = "Returns all students from the database"
    )
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {

        List<StudentDTO> students =
                studentService.getAllStudents()
                        .stream()
                        .map(StudentMapper::toDTO)
                        .toList();

        return ResponseEntity.ok(students);
    }

    // ==============================
    // GET STUDENTS - PAGINATION
    // ==============================

    @Operation(
            summary = "Get students with pagination",
            description = "Returns students using pagination and sorting"
    )
    @GetMapping("/page")
    public ResponseEntity<Page<StudentDTO>> getStudentsPage(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        Set<String> allowedSortFields =
                Set.of("id", "name", "course", "marks");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "id";
        }

        Sort sort;

        if ("desc".equalsIgnoreCase(direction)) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<StudentDTO> result =
                studentService
                        .getStudentsPage(pageable)
                        .map(StudentMapper::toDTO);

        return ResponseEntity.ok(result);
    }

    // ==============================
    // GET STUDENT BY ID
    // ==============================

    @Operation(
            summary = "Get student by ID",
            description = "Returns one student using the ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(
            @PathVariable Long id) {

        try {

            Student student =
                    studentService.getStudentById(id);

            StudentDTO dto =
                    StudentMapper.toDTO(student);

            return ResponseEntity.ok(dto);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    // ==============================
    // SEARCH STUDENTS
    // ==============================

    @Operation(
            summary = "Search students",
            description = "Searches students by name or course"
    )
    @GetMapping("/search")
    public ResponseEntity<List<StudentDTO>> searchStudents(
            @RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {

            return ResponseEntity.ok(List.of());
        }

        List<StudentDTO> students =
                studentService
                        .searchStudents(keyword.trim())
                        .stream()
                        .map(StudentMapper::toDTO)
                        .toList();

        return ResponseEntity.ok(students);
    }

    // ==============================
    // CREATE STUDENT
    // ==============================

    @Operation(
            summary = "Create student",
            description = "Creates a new student in the database"
    )
    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(
            @Valid @RequestBody StudentDTO dto) {

        Student student =
                StudentMapper.toEntity(dto);

        // Let the database generate the ID
        student.setId(null);

        Student savedStudent =
                studentService.saveStudent(student);

        StudentDTO response =
                StudentMapper.toDTO(savedStudent);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ==============================
    // UPDATE STUDENT
    // ==============================

    @Operation(
            summary = "Update student",
            description = "Updates an existing student"
    )
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO dto) {

        try {

            Student existingStudent =
                    studentService.getStudentById(id);

            existingStudent.setName(
                    dto.getName()
            );

            existingStudent.setCourse(
                    dto.getCourse()
            );

            existingStudent.setMarks(
                    dto.getMarks()
            );

            /*
             * Existing image is intentionally preserved.
             * No need to set it again.
             */

            Student updatedStudent =
                    studentService.saveStudent(
                            existingStudent
                    );

            StudentDTO response =
                    StudentMapper.toDTO(updatedStudent);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    // ==============================
    // DELETE STUDENT
    // ==============================

    @Operation(
            summary = "Delete student",
            description = "Deletes a student using the ID"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        try {

            // Check whether student exists
            studentService.getStudentById(id);

            studentService.deleteStudent(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}