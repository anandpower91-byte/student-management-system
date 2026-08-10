package com.example.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.export.ExcelExporter;
import com.example.demo.pdf.PDFExporter;
import com.example.demo.service.EmailService;
import com.example.demo.service.GoogleSheetsService;
import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final GoogleSheetsService googleSheetsService;
    private final EmailService emailService;

    public StudentController(
            StudentService studentService,
            GoogleSheetsService googleSheetsService,
            EmailService emailService) {

        this.studentService = studentService;
        this.googleSheetsService = googleSheetsService;
        this.emailService = emailService;
    }

    // ================= GOOGLE SHEET SYNC =================

    @GetMapping("/sync")
    public String syncStudentsFromGoogleSheet(
            RedirectAttributes redirectAttributes) {

        try {

            List<Student> students =
                    googleSheetsService.getStudentsFromSheet();

            int added = 0;

            System.out.println("========== GOOGLE SHEET DATA ==========");

            for (Student student : students) {

                System.out.println(
                        student.getName() + " | " +
                        student.getCourse() + " | " +
                        student.getMarks()
                );

                studentService.saveStudent(student);
                added++;
            }

            System.out.println("=======================================");

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Sync completed successfully. Imported "
                            + added + " students."
            );

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Google Sheet Sync Failed: "
                            + e.getMessage()
            );
        }

        return "redirect:/students";
    }

    // ================= LIST STUDENTS =================

    @GetMapping
    public String listStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        // Prevent invalid page
        if (page < 0) {
            page = 0;
        }

        // Prevent invalid page size
        if (size <= 0) {
            size = 10;
        }

        // ================= SEARCH =================

        List<Student> students;

        if (keyword == null || keyword.trim().isEmpty()) {

            students = studentService.getAllStudents();

        } else {

            students = studentService.searchStudents(keyword);
        }

        // ================= SORT =================

        Comparator<Student> comparator;

        switch (sortBy.toLowerCase()) {

            case "name":

                comparator = Comparator.comparing(
                        Student::getName,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

                break;

            case "course":

                comparator = Comparator.comparing(
                        Student::getCourse,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

                break;

            case "marks":

                comparator = Comparator.comparing(
                        Student::getMarks,
                        Comparator.nullsLast(
                                Integer::compareTo
                        )
                );

                break;

            case "id":
            default:

                comparator = Comparator.comparing(
                        Student::getId,
                        Comparator.nullsLast(
                                Long::compareTo
                        )
                );

                break;
        }

        // Descending order
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        students.sort(comparator);

        // ================= PAGINATION =================

        int totalStudents = students.size();

        int totalPages;

        if (totalStudents == 0) {
            totalPages = 0;
        } else {
            totalPages =
                    (int) Math.ceil(
                            (double) totalStudents / size
                    );
        }

        // Prevent page from going beyond last page
        if (totalPages > 0 && page >= totalPages) {
            page = totalPages - 1;
        }

        int start = page * size;

        int end = Math.min(
                start + size,
                totalStudents
        );

        List<Student> pageStudents;

        if (start >= totalStudents) {

            pageStudents = List.of();

        } else {

            pageStudents =
                    students.subList(start, end);
        }

        // ================= PAGE INFORMATION =================

        boolean hasPrevious = page > 0;

        boolean hasNext =
                totalPages > 0 &&
                page < totalPages - 1;

        // ================= SEND DATA TO THYMELEAF =================

        model.addAttribute(
                "students",
                pageStudents
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "pageSize",
                size
        );

        model.addAttribute(
                "totalPages",
                totalPages
        );

        model.addAttribute(
                "totalStudents",
                totalStudents
        );

        model.addAttribute(
                "sortBy",
                sortBy
        );

        model.addAttribute(
                "direction",
                direction
        );

        model.addAttribute(
                "hasPrevious",
                hasPrevious
        );

        model.addAttribute(
                "hasNext",
                hasNext
        );

        return "students";
    }

    // ================= ADD STUDENT FORM =================

    @GetMapping("/new")
    public String showCreateForm(Model model) {

        model.addAttribute(
                "student",
                new Student()
        );

        return "student-form";
    }
    // ================= SAVE STUDENT =================
    @PostMapping
    public String saveStudent(
            @Valid @ModelAttribute("student") Student student,
            BindingResult result,
            @RequestParam(
                    value = "image",
                    required = false
            ) MultipartFile image) {
        if (result.hasErrors()) {
            return "student-form";
        }
        try {
            if (image != null && !image.isEmpty()) {
                String uploadDir = "uploads/";
                Files.createDirectories(
                        Paths.get(uploadDir)
                );
                String originalFileName =
                        image.getOriginalFilename();
                String fileName =
                        System.currentTimeMillis()
                                + "_"
                                + originalFileName;
                Path path =
                        Paths.get(
                                uploadDir + fileName
                        );
                Files.copy(
                        image.getInputStream(),
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                );
                student.setImageName(fileName);
                System.out.println(
                        "IMAGE RECEIVED: "
                                + originalFileName
                );
                System.out.println(
                        "IMAGE SAVED AS: "
                                + fileName
                );
            } else {
                System.out.println(
                        "NO IMAGE RECEIVED"
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Student savedStudent =
                studentService.saveStudent(student);
        emailService.sendStudentAddedEmail(
                savedStudent.getName(),
                savedStudent.getCourse(),
                savedStudent.getMarks()
        );
        return "redirect:/students";
    }
    // ================= EDIT STUDENT =================
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("id") Long id,
            Model model) {
        Student student =
                studentService.getStudentById(id);
        model.addAttribute(
                "student",
                student
        );
        return "student-form";
    }
    // ================= VIEW STUDENT =================
    @GetMapping("/{id}")
    public String showStudentDetails(
            @PathVariable Long id,
            Model model) {
        Student student =
                studentService.getStudentById(id);
        model.addAttribute(
                "student",
                student
        );
        model.addAttribute(
                "performanceStatus",
                studentService.getPerformanceStatus(
                        student
                )
        );
        return "student-details";
    }
    // ================= DELETE STUDENT =================
    @GetMapping("/delete/{id}")
    public String deleteStudent(
            @PathVariable Long id) {
        Student student =
                studentService.getStudentById(id);
        // Delete image file
        if (student.getImageName() != null
                && !student.getImageName().isBlank()) {
            try {
                Path imagePath =
                        Paths.get(
                                "uploads",
                                student.getImageName()
                        );
                Files.deleteIfExists(
                        imagePath
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
    // ================= EXPORT EXCEL =================
    @GetMapping("/export/excel")
    public void exportToExcel(
            HttpServletResponse response)
            throws IOException {
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        String currentDateTime =
                new SimpleDateFormat(
                        "yyyy-MM-dd_HH-mm-ss"
                ).format(new Date());
        String headerValue =
                "attachment; filename=students_"
                        + currentDateTime
                        + ".xlsx";
        response.setHeader(
                "Content-Disposition",
                headerValue
        );
        List<Student> students =
                studentService.getAllStudents();
        ExcelExporter exporter =
                new ExcelExporter(students);
        exporter.export(response);
    }
    // ================= EXPORT PDF =================
    @GetMapping("/export/pdf")
    public void exportToPDF(
            HttpServletResponse response)
            throws IOException, DocumentException {
        response.setContentType(
                "application/pdf"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=students.pdf"
        );
        List<Student> students =
                studentService.getAllStudents();
        PDFExporter exporter =
                new PDFExporter(students);
        exporter.export(response);
    }
}