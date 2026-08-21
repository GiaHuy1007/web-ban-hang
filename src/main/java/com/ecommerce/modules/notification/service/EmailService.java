package com.ecommerce.modules.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@electronics-store.com}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Chào mừng bạn đến với Hệ thống E-commerce Thiết Bị Điện Tử!";
        String content = "<h3>Xin chào " + fullName + ",</h3>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản tại cửa hàng của chúng tôi. Chúc bạn có trải nghiệm mua sắm công nghệ tuyệt vời nhất!</p>";
        sendHtmlEmail(toEmail, subject, content);
    }

    @Async
    public void sendOrderConfirmationEmail(String toEmail, String orderNo, BigDecimal totalAmount) {
        String subject = "Xác nhận đơn hàng #" + orderNo + " thành công";
        String content = "<h3>Xin chào,</h3>"
                + "<p>Đơn hàng <b>#" + orderNo + "</b> của bạn đã được tiếp nhận thành công với tổng giá trị: <b>" + totalAmount + " VNĐ</b>.</p>"
                + "<p>Chúng tôi sẽ nhanh chóng chuẩn bị và gửi hàng đến bạn.</p>";
        sendHtmlEmail(toEmail, subject, content);
    }

    @Async
    public void sendPaymentSuccessEmail(String toEmail, String orderNo, BigDecimal amount, String transactionId) {
        String subject = "Thanh toán thành công đơn hàng #" + orderNo;
        String content = "<h3>Cảm ơn quý khách đã thanh toán!</h3>"
                + "<p>Đơn hàng <b>#" + orderNo + "</b> đã được thanh toán thành công số tiền <b>" + amount + " VNĐ</b>.</p>"
                + "<p>Mã giao dịch: <code>" + transactionId + "</code></p>";
        sendHtmlEmail(toEmail, subject, content);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}: subject='{}'", toEmail, subject);
        } catch (Exception e) {
            log.warn("Failed to send email to {} via SMTP (Simulated in local): error={}", toEmail, e.getMessage());
        }
    }
}
