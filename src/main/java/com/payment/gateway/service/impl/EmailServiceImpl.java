package com.payment.gateway.service.impl;

import com.payment.gateway.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendPaymentSuccessEmail(String to, String refId, Integer amount) {
        Context context = new Context();
        context.setVariable("refId", refId);
        context.setVariable("amount", amount);
        String html = templateEngine.process("email/success-email", context);
        sendHtmlEmail(to, "رسید پرداخت موفق", html);
    }

    @Override
    public void sendPaymentFailedEmail(String to, String reason) {
        Context context = new Context();
        context.setVariable("reason", reason);
        String html = templateEngine.process("email/failed-email", context);
        sendHtmlEmail(to, "پرداخت ناموفق", html);
    }

    @Override
    public void sendPaymentLinkEmail(String to, String paymentUrl) {
        Context context = new Context();
        context.setVariable("paymentUrl", paymentUrl);
        String html = templateEngine.process("email/payment-link", context);
        sendHtmlEmail(to, "لینک پرداخت شما", html);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("❌ خطا در ارسال ایمیل: " + e.getMessage());
        }
    }
}
