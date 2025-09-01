package com.ryanshiun.seniorscare.member.rowmapper.employee;

import com.ryanshiun.seniorscare.member.dto.employee.EmployeeProfileDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class EmployeeQueryMapper implements RowMapper<EmployeeProfileDto> {

    @Override
    public EmployeeProfileDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        EmployeeProfileDto employee = new EmployeeProfileDto();

        employee.setEmpId(rs.getInt("emp_id"));
        employee.setEmpName(rs.getString("emp_name"));
        employee.setEmail(rs.getString("email"));
        employee.setIsActive(rs.getBoolean("is_active"));
        employee.setImagePath(rs.getString("image_path"));
        employee.setDescribe(rs.getString("describe"));
        employee.setCreatedAt(rs.getTimestamp("created_at"));
        employee.setUpdatedAt((rs.getTimestamp("updated_at").toLocalDateTime()));
        return employee;
    }
}
