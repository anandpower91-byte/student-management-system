package com.example.demo.export;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.example.demo.Student;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

public class ExcelExporter {

    private final List<Student> students;

    public ExcelExporter(List<Student> students) {
        this.students = students;
    }

    public void export(HttpServletResponse response) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Course");
        header.createCell(3).setCellValue("Marks");

        int rowCount = 1;

        for (Student student : students) {

            Row row = sheet.createRow(rowCount++);

            row.createCell(0).setCellValue(student.getId());

            row.createCell(1).setCellValue(student.getName());

            row.createCell(2).setCellValue(student.getCourse());

            row.createCell(3).setCellValue(student.getMarks());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);

        ServletOutputStream outputStream = response.getOutputStream();

        workbook.write(outputStream);

        workbook.close();

        outputStream.close();
    }
}