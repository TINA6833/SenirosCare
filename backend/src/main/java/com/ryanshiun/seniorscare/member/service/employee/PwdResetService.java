package com.ryanshiun.seniorscare.member.service.employee;

import com.ryanshiun.seniorscare.member.dto.employee.PwdResetDto;
import jakarta.mail.MessagingException;

public interface PwdResetService {

    /*
     * 忘記密碼請求
     * 這個方法會產生一個6位數字的驗證碼，並寄送到使用者的電子郵件地址。
     * @param empId 員工ID，用於識別使用者
     */
    void requestReset(String email) throws MessagingException;

    /*
     * 驗證與重設密碼
     * 這個方法會檢查使用者提供的驗證碼是否正確。
     * @param email 使用者的電子郵件地址
     * @param code 使用者提供的驗證碼
     * @return 如果驗證碼正確，返回true；否則返回false。
     */
    void validateCode(String email, PwdResetDto pwdResetDto) throws RuntimeException;


    // 修改密碼
    void changePassword(Integer empId, String newPassword) throws RuntimeException;
}


