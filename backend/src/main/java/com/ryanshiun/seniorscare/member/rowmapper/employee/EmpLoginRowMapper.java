package com.ryanshiun.seniorscare.member.rowmapper.employee;

import com.ryanshiun.seniorscare.member.model.employee.Employee;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpLoginRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee emp = new Employee();
        emp.setEmpId(rs.getInt("emp_id"));
        emp.setEmpName(rs.getString("emp_name"));
        emp.setPassword(rs.getString("password"));
        emp.setEmail(rs.getString("email"));
        emp.setActive(rs.getBoolean("is_active"));
        return emp;
    }
}
