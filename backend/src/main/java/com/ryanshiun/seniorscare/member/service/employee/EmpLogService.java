package com.ryanshiun.seniorscare.member.service.employee;

import com.ryanshiun.seniorscare.member.model.employee.EmpLog;

import java.util.List;

public interface EmpLogService {
    void record(String empName, String action, String targetName);

    List<EmpLog> getAllLogs();
}
