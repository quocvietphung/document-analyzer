package orgaplan.beratung.kreditunterlagen.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String startPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("ub@orgaplan-beratung.de");
        helper.setTo(toEmail);
        helper.setSubject("Welcome to Orgaplan Beratung");

        String htmlContent = generateHtmlContent(toEmail,startPassword);
        helper.setText(htmlContent, true);

        ClassPathResource iconResource = new ClassPathResource("public/assets/images/icon.png");
        helper.addInline("KV-Logo", iconResource);

        mailSender.send(message);
    }

    private String generateHtmlContent(String toEmail, String startPassword) {
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 600px; margin: auto; background-color: #f4f4f4; border-radius: 8px;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<img src='cid:KV-Logo' alt='Orgaplan Beratung' style='max-width: 150px;' />"
                + "</div>"
                + "<h2 style='font-weight: bold; text-align: center;'>Herzlich willkommen bei App KreditDocs!</h2>"
                + "<p style='font-size: 16px; line-height: 1.5; text-align: center;'>Vielen Dank für Ihre Registrierung bei uns!</p>"
                + "<div style='text-align: center; margin-top: 30px;'>"
                + "<p style='font-size: 16px; line-height: 1.5; text-align: center;'>Nutzername: " + toEmail + "</p>"
                + "<p style='font-size: 16px; line-height: 1.5; text-align: center;'>Passwort: " + startPassword + "</p>"
                + "<div style='text-align: center; margin-top: 30px;'>"
                + "<a href='' style='background-color: #007BFF; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold;'>Zur App</a>"
                + "</div>"
                + "<p style='font-size: 14px; text-align: center; margin-top: 20px; color: #888;'>Wenn Sie sich nicht bei KreditDocs registriert haben, ignorieren Sie bitte diese E-Mail.</p>"
                + "<div style='margin-top: 40px; border-top: 1px solid #ddd; padding-top: 20px;'>"
                + "<p style='font-size: 14px; text-align: center; color: #666;'>"
                + "<a href='https://orgaplan-beratung.de/impressum' style='color: #007BFF; text-decoration: none;'>Impressum</a>"
                + "</p>"
                + "<p style='font-size: 14px; text-align: center; color: #666;'>"
                + "<a href='https://orgaplan-beratung.de/datenschutz' style='color: #007BFF; text-decoration: none;'> Datenschutzrichtlinien</a>"
                + "</p>"
                + "</div>"
                + "</div>";
    }
}
