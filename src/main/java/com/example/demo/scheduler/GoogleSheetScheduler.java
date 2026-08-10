package com.example.demo.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.Student;
import com.example.demo.StudentService;
import com.example.demo.service.GoogleSheetsService;

@Component
public class GoogleSheetScheduler {

    private final GoogleSheetsService googleSheetsService;
    private final StudentService studentService;

    public GoogleSheetScheduler(
            GoogleSheetsService googleSheetsService,
            StudentService studentService) {

        this.googleSheetsService = googleSheetsService;
        this.studentService = studentService;
    }

    // ==============================
    // AUTO GOOGLE SHEET SYNC
    // Runs every 1 minute
    // ==============================

    @Scheduled(fixedRate = 60000)
    public void autoSync() {

        System.out.println(
                "========== AUTO SYNC STARTED =========="
        );

        try {

            List<Student> students =
                    googleSheetsService.getStudentsFromSheet();

            System.out.println(
                    "Students received from Google Sheet: "
                            + students.size()
            );

            for (Student sheetStudent : students) {

                if (sheetStudent.getName() == null ||
                        sheetStudent.getName().trim().isEmpty()) {
                    continue;
                }

                // Check whether this student already exists
                Student existingStudent =
                        studentService.findByName(
                                sheetStudent.getName()
                        );

                if (existingStudent != null) {

                    // Update existing student
                    existingStudent.setCourse(
                            sheetStudent.getCourse()
                    );

                    existingStudent.setMarks(
                            sheetStudent.getMarks()
                    );

                    studentService.saveStudent(
                            existingStudent
                    );

                    System.out.println(
                            "UPDATED: "
                                    + existingStudent.getName()
                    );

                } else {

                    // Add new Google Sheet student
                    studentService.saveStudent(
                            sheetStudent
                    );

                    System.out.println(
                            "ADDED: "
                                    + sheetStudent.getName()
                    );
                }
            }

            System.out.println(
                    "========== AUTO SYNC COMPLETED =========="
            );

        } catch (Exception e) {

            System.out.println(
                    "========== AUTO SYNC FAILED =========="
            );

            e.printStackTrace();
        }
    }
}