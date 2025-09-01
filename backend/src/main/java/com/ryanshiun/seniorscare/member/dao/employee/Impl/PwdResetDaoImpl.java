package com.ryanshiun.seniorscare.member.dao.employee.Impl;

import com.ryanshiun.seniorscare.member.dao.employee.PwdResetDao;
import com.ryanshiun.seniorscare.member.model.employee.PwdReset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PwdResetDaoImpl implements PwdResetDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // 產生新驗證碼
    @Override
    public Integer insert(PwdReset pwdReset) {
        final String sql = "INSERT INTO pwdReset (emp_id, code, expires_at) VALUES (:empId, :code, :expires_at)";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", pwdReset.getEmpId());
        map.put("code", pwdReset.getCode());
        map.put("expires_at", pwdReset.getExpiresAt());
       return namedParameterJdbcTemplate.update(sql, map);
    }

    // 取得驗證碼資訊
    @Override
    public String getValidCode(Integer empId) {
            final String sql = "SELECT TOP 1 code" +
                            " FROM pwdReset" +
                            " WHERE emp_Id = :empId" +
                            " AND used = 0" +
                            " AND expires_at > GETDATE()" +
                            " ORDER BY expires_at DESC";
            Map<String, Object> map = new HashMap<>();
            map.put("empId", empId);
        return namedParameterJdbcTemplate.queryForObject(sql, map, String.class);
    }

    @Override
    public Integer markUsed(Integer empId) {
        final String sql = "UPDATE pwdReset SET used = 1 WHERE emp_id = :empId AND used = 0";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);
        return namedParameterJdbcTemplate.update(sql, map);
    }
}
