package com.cocoon.implementation;

import com.cocoon.service.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmailWithAttachment(String from, String to, String subject, String body, byte[] content) {

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment("invoice.pdf", new ByteArrayResource(content));
            mailSender.send(message);
        } catch (MessagingException e) {

            e.printStackTrace();
        }
    }

}
