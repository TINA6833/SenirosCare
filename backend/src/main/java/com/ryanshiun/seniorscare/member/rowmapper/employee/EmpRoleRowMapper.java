package com.ryanshiun.seniorscare.member.rowmapper.employee;

import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpRoleRowMapper implements RowMapper<EmpRole> {

    @Override
    public EmpRole mapRow(ResultSet rs, int rowNum) throws SQLException {
        EmpRole empRole = new EmpRole();

        empRole.setRoleId(rs.getInt("role_id"));
        empRole.setRoleName(rs.getString("role_name"));
        return empRole;
    }
}
