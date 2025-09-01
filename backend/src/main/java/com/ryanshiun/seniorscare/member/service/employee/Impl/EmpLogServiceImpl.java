package com.ryanshiun.seniorscare.member.service.employee.Impl;

import com.ryanshiun.seniorscare.member.dao.employee.EmpLogDao;
import com.ryanshiun.seniorscare.member.model.employee.EmpLog;
import com.ryanshiun.seniorscare.member.service.employee.EmpLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmpLogServiceImpl implements EmpLogService {
    @Autowired
    private EmpLogDao empLogDao;

    @Override
    public void record(String empName, String action, String targetName) {
        EmpLog empLog = new EmpLog();
        empLog.setEmpName(empName);
        empLog.setAction(action);
        empLog.setTargetName(targetName);
        empLogDao.insert(empLog);
    }

    @Override
    public List<EmpLog> getAllLogs() {
        return empLogDao.getAllLogs();
    }
}
