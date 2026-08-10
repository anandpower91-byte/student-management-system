package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendStudentAddedEmail(String studentName,
                                      String course,
                                      Integer marks) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(fromEmail);
            message.setSubject("New Student Added");

            message.setText(
                    "A new student has been added.\n\n" +
                    "Name   : " + studentName + "\n" +
                    "Course : " + course + "\n" +
                    "Marks  : " + marks
            );

            System.out.println("Sending email...");

            mailSender.send(message);

            System.out.println("Email sent successfully.");

        } catch (Exception e) {

            System.out.println("==================================");
            System.out.println("EMAIL SENDING FAILED");
            System.out.println(e.getMessage());
            System.out.println("==================================");

            e.printStackTrace();
        }
    }
}