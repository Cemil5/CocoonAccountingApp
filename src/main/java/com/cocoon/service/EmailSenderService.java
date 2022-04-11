package com.cocoon.service;

public interface EmailSenderService {

    void sendEmailWithAttachment(String from, String to, String subject, String body, byte[] content);
}
