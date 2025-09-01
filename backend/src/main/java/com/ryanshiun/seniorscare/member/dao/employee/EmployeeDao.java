package com.ryanshiun.seniorscare.member.dao.employee;

import com.ryanshiun.seniorscare.member.dto.employee.EmployeeCreateDto;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeProfileDto;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeQueryParamsDto;
import com.ryanshiun.seniorscare.member.dto.employee.EmployeeUpdateDto;
import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import com.ryanshiun.seniorscare.member.model.employee.Employee;

import java.util.List;

public interface EmployeeDao {
    // 查詢單一員工資料 By empId
    EmployeeProfileDto getEmployeeById(int empId);

    // 增加後台員工
    Integer addEmployee(EmployeeCreateDto employeeCreateDto);

    // 賦予初始權限
    void assignInitialRole(int empId, int roleId);

    // 啟用 or 停權員工
    void toggleEmployeeStatus(int empId, boolean isActive);

    // 修改密碼
    void updatePwd (int empId, String newPwd);

    // 修改員工資訊 By empId
    void updateEmployee(int empId, EmployeeUpdateDto employeeUpdateDto);

    // 條件查詢員工 (模糊查詢、停權名單等)
    List<EmployeeProfileDto> getEmployees(EmployeeQueryParamsDto empQueryParams);

    // 查詢所有職等
    List<EmpRole> getAllRoles();

    // 查詢該員工所有權限
    List<EmpRole> getEmployeeRoles(int empId);

    // 刪除該員工所有權限
    void deleteEmployeeRoles(int empId);

    // 新增員工權限
    void addEmployeeRole(int empId, List<Integer> newRoleIds);

    // 查詢員工資料 (登入用)
    Employee getEmployeeByEmail(String email);

    // 查詢員工資料 (前端用)
    EmployeeProfileDto passEmpInfo(String email);
}
