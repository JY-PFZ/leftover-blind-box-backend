package nus.iss.se.magicbag.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.common.constant.RedisPrefix;
import nus.iss.se.magicbag.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    @Getter
    @Value("${common.url}")
    private String url;

    private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;

    private static final Random RANDOM = new Random();
    private static final String ACTIVATE_API = "api/auth/activate";

    // 发送验证码邮件（HTML 格式）
    public void sendVerificationCode1(String to, String code) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("验证账户邮箱");

        // 构建 HTML 内容
        String htmlContent = """
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; line-height: 1.6; padding: 20px; }
                    h1 { text-align: center; color: #222; margin-bottom: 20px; }
                    .code { font-size: 32px; font-weight: bold; color: #000; margin: 30px 0; }
                    .note { font-size: 14px; margin: 20px 0; }
                    .reason { font-weight: bold; margin-top: 20px; }
                </style>
            </head>
            <body>
                <h1>验证账户邮箱</h1>
                <p>你已选择此电子邮件地址作为你的账户。为验证此电子邮件地址属于你，请在验证页面输入下方验证码：</p>
                <div class="code">%s</div>
                <p class="note">此电子邮件发出 3 小时后，验证码将过期。</p>
                <p class="reason">你收到此电子邮件的原因：</p>
                <p>系统会在你选择该邮箱时提出验证要求。你的账户需经过验证才能使用。</p>
                <p>如果你未提出此请求，可以忽略这封电子邮件。未经过验证便无法创建账户。</p>
            </body>
            </html>
            """.formatted(code);

        helper.setText(htmlContent, true); // true 表示是 HTML

        javaMailSender.send(message);
        log.info("验证码邮件已发送至: {}",to);
    }

    public void sendActivationEmail(String to) throws MessagingException {
        // 1. 构建激活链接
        String token = UUID.randomUUID().toString();
        String activateUrl = generateActivateLink(token);

        // 2. 保存 token 到Redis（有效期 3 小时）
        redisUtil.set(RedisPrefix.ACCOUNT_ACTIVATE_TOKEN.getCode() + token, to, 3, TimeUnit.HOURS);

        // 3.发送邮件
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Activate your MagicBag Account");

        String htmlContent = activateMailContent(to,activateUrl);
        helper.setText(htmlContent, true);
        javaMailSender.send(message);

        log.info("验证码链接已发送至: {}",to);
    }

    public static String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    private String activateMailContent(String to, String activateUrl){
        return  """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        color: #333;
                        line-height: 1.6;
                        margin: 0;
                        padding: 20px;
                        background-color: #f5f7fa;
                    }
                    .container {
                        max-width: 600px;
                        margin: auto;
                        background: white;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 {
                        color: #2c3e50;
                        margin-top: 0;
                    }
                    p {
                        margin-bottom: 16px;
                    }
                    .button {
                        display: block;
                        width: 300px;
                        margin: 40px auto;
                        padding: 14px;
                        background-color: #4a90e2;
                        color: white;
                        text-align: center;
                        text-decoration: none;
                        font-size: 18px;
                        font-weight: bold;
                        border-radius: 8px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .link {
                        color: #1a73e8;
                        text-decoration: underline;
                    }
                    .footer {
                        margin-top: 30px;
                        font-size: 14px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Activate your MagicBag Account</h1>
                    <p>Thank you for joining MagicBag.</p>
                    <p>You entered <a href="mailto:%s" class="link">%s</a> as your MagicBag Account. To verify your email, click "Activate Now" to confirm your email address:</p>
                    <a href="%s" class="button">Activate Now</a>
                    <p>Or copy and paste the following link into your browser:<br>
                        <a href="%s" class="link">%s</a>
                    </p>
                    <p>The link will expire in 3 hours.</p>
                    <p>If it wasn't you, please ignore this message.</p>

                    <div class="footer">
                        Best Regards,<br>
                        MagicBag Group
                    </div>
                </div>
            </body>
            </html>
            """.formatted(to, to, activateUrl, activateUrl, activateUrl);
    }

    private String generateActivateLink(String token){
        return url + "/" + ACTIVATE_API + "?token=" + token;
    }
}
