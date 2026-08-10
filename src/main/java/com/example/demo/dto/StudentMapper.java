package com.example.demo.dto;

import com.example.demo.Student;

public final class StudentMapper {

    private StudentMapper() {
        // Utility class
    }

    // ==============================
    // ENTITY -> DTO
    // ==============================

    public static StudentDTO toDTO(Student student) {

        if (student == null) {
            return null;
        }

        return new StudentDTO(
                student.getId(),
                student.getName(),
                student.getCourse(),
                student.getMarks(),
                student.getImageName()
        );
    }

    // ==============================
    // DTO -> ENTITY
    // ==============================

    public static Student toEntity(StudentDTO dto) {

        if (dto == null) {
            return null;
        }

        Student student = new Student();

        student.setId(dto.getId());
        student.setName(dto.getName());
        student.setCourse(dto.getCourse());
        student.setMarks(dto.getMarks());
        student.setImageName(dto.getImageName());

        return student;
    }
}