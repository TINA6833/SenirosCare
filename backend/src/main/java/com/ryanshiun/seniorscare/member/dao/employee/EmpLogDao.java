package com.ryanshiun.seniorscare.member.dao.employee;

import com.ryanshiun.seniorscare.member.model.employee.EmpLog;

import java.util.List;

public interface EmpLogDao {
    // 新增紀錄
    Integer insert(EmpLog empLog);

    // 取得全部紀錄
    List<EmpLog> getAllLogs();
}
