package com.ryanshiun.seniorscare.member.service.employee.Impl;

import com.ryanshiun.seniorscare.member.dao.employee.EmployeeDao;
import com.ryanshiun.seniorscare.member.dao.employee.PwdResetDao;
import com.ryanshiun.seniorscare.member.dto.employee.PwdResetDto;
import com.ryanshiun.seniorscare.member.model.employee.Employee;
import com.ryanshiun.seniorscare.member.model.employee.PwdReset;
import com.ryanshiun.seniorscare.member.service.employee.PwdResetService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Properties;

@Component
public class PwdResetServiceImpl implements PwdResetService {
    @Autowired
    PwdResetDao pwdResetDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    EmployeeDao employeeDao;

    // 電子郵件設定
    @Value("${spring.mail.host}")
    String smtpHost;

    @Value("${spring.mail.port}")
    private String smtpPort;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Transactional
    @Override
    public void requestReset(String email) {
        // 將驗證碼資訊寫入資料庫
        Employee employee = employeeDao.getEmployeeByEmail(email);
        String opt = String.format("%06d", new SecureRandom().nextInt(1000000));
        String targetEmail = employee.getEmail();
        // 10 分鐘後過期
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(600);
        PwdReset pwdReset = new PwdReset();
        pwdReset.setEmpId(employee.getEmpId());
        pwdReset.setCode(opt);
        pwdReset.setExpiresAt(expireTime);
        pwdResetDao.insert(pwdReset);

        // 寄送驗證碼到使用者的電子郵件
        try {
            if (targetEmail == null || targetEmail.isEmpty()) {
                throw new IllegalArgumentException("Employee not found or email is empty");
            }
            sendEmail(targetEmail, opt);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Transactional
    @Override
    public void validateCode(String email, PwdResetDto pwdResetDto) {
        // 根據 email 查詢員工 ID
        Employee employee = employeeDao.getEmployeeByEmail(email);
        Integer empId = employee.getEmpId();
        String opt = pwdResetDao.getValidCode(empId);
        // 檢查 opt 驗證碼是否正確
        if (opt == null || !opt.equals(pwdResetDto.getCode())) {
            throw new IllegalArgumentException("Invalid code");
        }
        String encodedPassword = passwordEncoder.encode(pwdResetDto.getNewPwd());

        // 更新員工密碼
        employeeDao.updatePwd(empId, encodedPassword);
        // 標記驗證碼已使用
        pwdResetDao.markUsed(empId);
    }

    // 寄驗證碼給使用者
    private void sendEmail(String targetEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(from, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail));
        message.setSubject("重設密碼驗證碼");
        message.setText("您的驗證碼為：" + code + "，請在 10 分鐘內使用。");
        Transport.send(message);
    }

    @Override
    public void changePassword(Integer empId, String newPassword) throws RuntimeException {
        String encodedPassword = passwordEncoder.encode(newPassword);
        employeeDao.updatePwd(empId, encodedPassword);
    }
}
