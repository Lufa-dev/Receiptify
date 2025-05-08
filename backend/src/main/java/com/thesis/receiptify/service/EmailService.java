package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Profile;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${app.url}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send a simple text email
     *
     * @param to recipient email address
     * @param subject email subject
     * @param text email body text
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Receiptify <" + fromEmail + ">");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Async
    public void sendWelcomeEmail(Profile profile) {
        try {
            String name = profile.getFirstName() != null ? profile.getFirstName() : profile.getUsername();

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Hi ").append(name).append(",\n\n");
            emailBody.append("Welcome to Receiptify! We're excited to have you join our community.\n\n");
            emailBody.append("With Receiptify, you can:\n");
            emailBody.append("- Create and save your own recipes\n");
            emailBody.append("- Organize recipes into collections\n");
            emailBody.append("- Discover seasonal recipes\n");
            emailBody.append("- Rate and comment on recipes\n\n");
            emailBody.append("Start exploring now at: ").append(appUrl).append("\n\n");
            emailBody.append("Happy cooking!\n\n");
            emailBody.append("The Receiptify Team\n\n");
            emailBody.append("--\n");
            emailBody.append("This email was sent from a no-reply address. Please do not reply to this message.");

            // Send the welcome email
            sendSimpleMessage(
                    profile.getEmail(),
                    "Welcome to Receiptify!",
                    emailBody.toString());
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Error sending welcome email: " + e.getMessage());
        }
    }
}
