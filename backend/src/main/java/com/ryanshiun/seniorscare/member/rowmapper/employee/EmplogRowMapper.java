package com.ryanshiun.seniorscare.member.rowmapper.employee;

import com.ryanshiun.seniorscare.member.model.employee.EmpLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class EmplogRowMapper implements RowMapper<EmpLog> {
    @Override
    public EmpLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        EmpLog empLog = new EmpLog();

        empLog.setEmpName(rs.getString("emp_name"));
        empLog.setAction(rs.getString("action"));
        empLog.setTargetName(rs.getString("target_name"));
        empLog.setCreatedAt(LocalDateTime.parse(String.valueOf(rs.getTimestamp("created_at"))));
        return empLog;
    }
}
