package com.example.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.Student;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME =
            "Student Management System";

    private static final String CREDENTIALS_FILE_PATH =
            "src/main/resources/credentials.json";

    // Your Google Sheet ID
    private static final String SPREADSHEET_ID =
            "1u5A2y5FqxjWwmQOiAHuRcHOinNeSMAAwwqPseeKuFN8";

    // Sheet name and columns
    // A = Name
    // B = Course
    // C = Marks
    private static final String RANGE =
            "Students!A2:C";

    public Sheets getSheetsService()
            throws IOException, GeneralSecurityException {

        GoogleCredentials credentials =
                GoogleCredentials
                        .fromStream(
                                new FileInputStream(
                                        CREDENTIALS_FILE_PATH
                                )
                        )
                        .createScoped(
                                Collections.singleton(
                                        SheetsScopes.SPREADSHEETS
                                )
                        );

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
        .setApplicationName(APPLICATION_NAME)
        .build();
    }

    public List<Student> getStudentsFromSheet()
            throws Exception {

        Sheets service = getSheetsService();

        ValueRange response =
                service.spreadsheets()
                        .values()
                        .get(
                                SPREADSHEET_ID,
                                RANGE
                        )
                        .execute();

        List<List<Object>> values =
                response.getValues();

        List<Student> students =
                new ArrayList<>();

        if (values == null || values.isEmpty()) {
            return students;
        }

        for (List<Object> row : values) {

            // Ignore completely empty rows
            if (row == null || row.isEmpty()) {
                continue;
            }

            Student student = new Student();

            // NAME
            if (row.size() > 0 &&
                    row.get(0) != null) {

                student.setName(
                        row.get(0).toString().trim()
                );
            }

            // COURSE
            if (row.size() > 1 &&
                    row.get(1) != null) {

                student.setCourse(
                        row.get(1).toString().trim()
                );
            }

            // MARKS
            if (row.size() > 2 &&
                    row.get(2) != null &&
                    !row.get(2).toString().trim().isEmpty()) {

                try {

                    student.setMarks(
                            Integer.parseInt(
                                    row.get(2)
                                            .toString()
                                            .trim()
                            )
                    );

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Invalid marks value in Google Sheet: "
                                    + row.get(2)
                    );

                    continue;
                }
            }

            // Only add rows that have a name
            if (student.getName() != null &&
                    !student.getName().isEmpty()) {

                students.add(student);
            }
        }

        return students;
    }
}