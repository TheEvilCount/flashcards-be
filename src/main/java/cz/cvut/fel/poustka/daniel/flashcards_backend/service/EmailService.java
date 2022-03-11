package cz.cvut.fel.poustka.daniel.flashcards_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService
{
    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender1)
    {
        this.javaMailSender = javaMailSender1;
    }

    @Async
    public void sendMail(String to, String subject, String body)
    {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        try
        {
            javaMailSender.send(msg);
        }
        catch (MailException e)
        {
            LOG.error("EmailService send email error: " + e);
            throw new MailSendException(e.toString());
        }

    }
}
