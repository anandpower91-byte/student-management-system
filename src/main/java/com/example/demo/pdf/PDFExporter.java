package com.example.demo.pdf;

import java.io.IOException;
import java.util.List;

import com.example.demo.Student;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

public class PDFExporter {

    private List<Student> students;

    public PDFExporter(List<Student> students) {
        this.students = students;
    }

    public void export(HttpServletResponse response)
            throws DocumentException, IOException {

        Document document = new Document();

        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);

        Paragraph title = new Paragraph("Student Management System", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        table.addCell(new PdfPCell(new Phrase("ID")));
        table.addCell(new PdfPCell(new Phrase("Name")));
        table.addCell(new PdfPCell(new Phrase("Course")));
        table.addCell(new PdfPCell(new Phrase("Marks")));

        for (Student student : students) {

            table.addCell(String.valueOf(student.getId()));
            table.addCell(student.getName());
            table.addCell(student.getCourse());
            table.addCell(String.valueOf(student.getMarks()));
        }

        document.add(table);

        document.close();
    }
}