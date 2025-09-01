package com.ryanshiun.seniorscare.member.controller.employee;

import com.ryanshiun.seniorscare.member.dto.employee.PwdResetDto;
import com.ryanshiun.seniorscare.member.service.employee.PwdResetService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/api/employees/pwdReset")
@RestController
public class PwdResetController {

    @Autowired
    private PwdResetService pwdResetService;

    /* 提出密碼重設需求
     * @param email 員工電子郵件
     */
    @PostMapping("/{email}")
    public ResponseEntity<?> requestReset(@PathVariable String email) {
        try {
            pwdResetService.requestReset(email);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (MessagingException e) {
//            throw new RuntimeException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /* 比對驗證碼是否正確
     * @param email 員工電子郵件
     * @param code 驗證碼
     */
    @PutMapping("/{email}")
    public ResponseEntity<?> validateCode(@PathVariable String email,
                             @RequestBody @Valid PwdResetDto pwdResetDto
    ) {
        try {
            pwdResetService.validateCode(email, pwdResetDto);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
