package com.ecommerce.project.services;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendSimpleMail(String to, String subject, String text);

    void sendHtmlEmail(String to, String subject, String text) throws MessagingException;

}
