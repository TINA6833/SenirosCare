package com.ryanshiun.seniorscare.member.dao.employee.Impl;

import com.ryanshiun.seniorscare.member.dao.employee.EmployeeDao;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeCreateDto;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeProfileDto;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeQueryParamsDto;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeUpdateDto;
import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import com.ryanshiun.seniorscare.member.model.employee.Employee;
import com.ryanshiun.seniorscare.member.rowmapper.employee.EmpLoginRowMapper;
import com.ryanshiun.seniorscare.member.rowmapper.employee.EmpRoleRowMapper;
import com.ryanshiun.seniorscare.member.rowmapper.employee.EmployeeQueryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class EmployeeDaoImpl implements EmployeeDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    LocalDateTime now = LocalDateTime.now();
    // 提取共用的 SQL 查詢員工語法
    private static final String BASE_EMPLOYEE_QUERY = """
        SELECT
          e.emp_id,
          e.emp_name,
          e.email,
          e.is_active,
          e.image_path,
          e.describe,
          e.created_at,
          e.updated_at
        FROM employee AS e
        """;

    /**
     * 新增員工
     * @param employeeCreateDto 註冊員工參數
     * @return 新員工 ID
     */
    @Override
    public Integer addEmployee(EmployeeCreateDto employeeCreateDto) {
        final String sql = "INSERT INTO employee (emp_name, password, email) VALUES (:empName, :password, :email)";
        Map<String, Object> map = new HashMap<>();
        map.put("empName", employeeCreateDto.getEmpName());
        map.put("password", employeeCreateDto.getPassword());
        map.put("email", employeeCreateDto.getEmail());

        // 儲存資料庫自動生成的 productId
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    /** 新增員工權限
     * @param empId 員工 ID
     * @param newRoleIds 新的權限 ID 列表
     */
    @Override
    public void addEmployeeRole(int empId, List<Integer> newRoleIds) {
        final String sql = "INSERT INTO emp_has_role (emp_id, role_id) VALUES (:empId, :roleId)";
        Map<String, Object> map = new HashMap<>();
        for (Integer roleId : newRoleIds) {
            map.put("empId", empId);
            map.put("roleId", roleId);
            namedParameterJdbcTemplate.update(sql, map);
        }
    }

    /** 指派初始職等給新員工
     * @param empId 員工 ID
     * @param roleId 職等 ID
     */
    @Override
    public void assignInitialRole(int empId, int roleId) {
        final String sql = "INSERT INTO emp_has_role (emp_id, role_id) VALUES (:empId, :roleId)";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);
        map.put("roleId", roleId);
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map));
    }

    /** 修改密碼
     * @param empId 員工 ID
     * @param newPwd 新的密碼
     */
    @Override
    public void updatePwd(int empId, String newPwd) {
        final String sql = "UPDATE employee SET password = :newPwd, updated_at = :updatedAt  WHERE emp_id = :empId";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);
        map.put("newPwd", newPwd);
        map.put("updatedAt", now);
        namedParameterJdbcTemplate.update(sql, map);
    }

    /** 更新員工資訊 By empId
     * @param employeeUpdateDto 員工更新資訊
     * @param empId 員工 ID
     */
    @Override
    public void updateEmployee(int empId, EmployeeUpdateDto employeeUpdateDto) {
        final String sql = "UPDATE employee SET emp_name = :empName, email = :email," +
                " image_path = :imagePath, describe = :describe, updated_at = :updatedAt  WHERE emp_id = :empId";
        Map<String, Object> map = new HashMap<>();
        map.put("empName", employeeUpdateDto.getEmpName());
        map.put("email", employeeUpdateDto.getEmail());
        map.put("imagePath", employeeUpdateDto.getImagePath());
        map.put("describe", employeeUpdateDto.getDescribe());
        map.put("empId", empId);
        map.put("updatedAt", now);
        namedParameterJdbcTemplate.update(sql, map);
    }

    /** 刪除該員工所有權限
     * @param empId 員工 ID
     */
    @Override
    public void deleteEmployeeRoles(int empId) {
        final String sql = "DELETE FROM emp_has_role WHERE emp_id = :empId";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);
        namedParameterJdbcTemplate.update(sql, map);
    }

    /** 停權或啟用員工
     * @param empId 員工 ID
     * @param isActive 停權/啟用狀態
     */
    @Override
    public void toggleEmployeeStatus(int empId, boolean isActive) {
        final String sql = "UPDATE employee SET is_active = :isActive, updated_at = :updatedAt WHERE emp_id = :empId";
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);
        map.put("isActive", isActive);
        map.put("updatedAt", now);
        namedParameterJdbcTemplate.update(sql, map);
    }

    /** 查詢所有職等
     * @return 回傳所有職位列表
     */
    @Override
    public List<EmpRole> getAllRoles() {
        final String sql = "SELECT DISTINCT role_id, role_name FROM emp_role";
        return namedParameterJdbcTemplate.query(sql, Collections.emptyMap(), new EmpRoleRowMapper());
    }

    /** 查詢單一員工資料 By empId
     * @param empId 員工 ID
     * @return 該員工詳細資料
     */
    @Override
    public EmployeeProfileDto getEmployeeById(int empId) {
        // 使用共用 SQL 並加上 WHERE 條件
        final String sql = BASE_EMPLOYEE_QUERY + " WHERE e.emp_id = :empId";

        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);

        // 先查詢員工
        List<EmployeeProfileDto> list =
                namedParameterJdbcTemplate.query(sql, map, new EmployeeQueryMapper());
        if (list.isEmpty()) return null;
        EmployeeProfileDto dto = list.get(0);

        // 再查詢該員工所有權限
        List<EmpRole> roles = getEmployeeRoles(empId);
        dto.setRoles(roles);
        return dto;
    }

    /** 客製化查詢員工 (根據職等、模糊查詢、停權名單等)
     *  使用共用 SQL 並加上 WHERE 1=1 以便拼接
     * @param empQueryParams 查詢參數 DTO (員工姓名、是否停權)
     * @return 符合條件的員工列表
     */
    @Override
    public List<EmployeeProfileDto> getEmployees(EmployeeQueryParamsDto empQueryParams) {

        StringBuilder sql = new StringBuilder(BASE_EMPLOYEE_QUERY);
        sql.append(" WHERE 1=1");

        Map<String, Object> map = new HashMap<>();

        if (empQueryParams.getEmpName() != null) {
            sql.append(" AND e.emp_name LIKE :empName");
            map.put("empName", "%" + empQueryParams.getEmpName() + "%");
        }

        if (empQueryParams.getIsActive() != null) {
            sql.append(" AND e.is_active = :isActive");
            map.put("isActive", empQueryParams.getIsActive());
        }

        List<EmployeeProfileDto> employees =
                namedParameterJdbcTemplate.query(sql.toString(), map, new EmployeeQueryMapper());
        if (employees.isEmpty()) return employees;

        // 對每個員工查詢他的所有權限
        for (EmployeeProfileDto dto : employees) {
            dto.setRoles(getEmployeeRoles(dto.getEmpId()));
        }
        return employees;
    }

    /** 查詢該員工所有權限
     * @param empId 員工 ID
     * @return 員工權限列表
     */
    @Override
    public List<EmpRole> getEmployeeRoles(int empId) {
        final String sql = """
                SELECT emp_role.role_id, emp_role.role_name FROM emp_role
                    JOIN emp_has_role ON emp_role.role_id = emp_has_role.role_id
                WHERE emp_has_role.emp_id = :empId
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("empId", empId);

        return namedParameterJdbcTemplate.query(sql, map, new EmpRoleRowMapper());
    }

    /** 查詢員工資料 (登入用)
     * @param email 員工信箱
     * @return 員工登入資料
     */
    @Override
    public Employee getEmployeeByEmail(String email) {
        final String sql = "SELECT emp_id, emp_name, password, email, is_active FROM employee" +
                " WHERE email = :email";
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        List<Employee> employeeList =  namedParameterJdbcTemplate.query(sql, map, new EmpLoginRowMapper());
        if (!employeeList.isEmpty()) {
            return employeeList.get(0);
        } else {
            return null;
        }
    }

    /** 查詢員工資料 (前端用)
     * @param email 員工信箱
     * @return 員工詳細資料
     */
    @Override
    public EmployeeProfileDto passEmpInfo(String email) {
        // 使用共用 SQL 並加上 WHERE 條件
        final String sql = BASE_EMPLOYEE_QUERY + " WHERE e.email = :email";

        Map<String, Object> map = new HashMap<>();
        map.put("email", email);

        // 先查詢員工
        List<EmployeeProfileDto> list =
                namedParameterJdbcTemplate.query(sql, map, new EmployeeQueryMapper());
        if (list.isEmpty()) return null;
        EmployeeProfileDto dto = list.get(0);

        // 再查詢該員工所有權限
        List<EmpRole> roles = getEmployeeRoles(dto.getEmpId());
        dto.setRoles(roles);
        return dto;
    }
}