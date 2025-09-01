package com.ryanshiun.seniorscare.member.dao.employee.Impl;

import com.ryanshiun.seniorscare.member.dao.employee.EmpLogDao;
import com.ryanshiun.seniorscare.member.model.employee.EmpLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EmpLogDaoImpl implements EmpLogDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // 新增一筆紀錄
    @Override
    public Integer insert(EmpLog empLog) {
       final String sql = "INSERT INTO emp_log (emp_name, action, target_name)" +
                       " VALUES (:empName, :action, :targetName)";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empLog.getEmpName());
        map.put("action", empLog.getAction());
        map.put("targetId", empLog.getTargetName());
        return namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public List<EmpLog> getAllLogs() {
        final String sql = "SELECT emp_name, action, target_name, created_at FROM emp_log";
        return namedParameterJdbcTemplate.queryForList(sql, Collections.emptyMap(), EmpLog.class);
    }
}
