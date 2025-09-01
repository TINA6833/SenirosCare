package com.ryanshiun.seniorscare.member.dao.employee;

import com.ryanshiun.seniorscare.member.model.employee.PwdReset;

public interface PwdResetDao {
    // 注入密碼
    Integer insert(PwdReset pwdReset);

    // 取得驗證碼
    String getValidCode(Integer empId);

    // 標記已使用該驗證碼
    Integer markUsed(Integer empId);
}
