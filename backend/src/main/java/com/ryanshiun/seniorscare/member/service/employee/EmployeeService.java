package com.ryanshiun.seniorscare.member.service.employee;

import com.ryanshiun.seniorscare.member.dto.employee.*;
import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import jakarta.validation.Valid;

import java.util.List;

public interface EmployeeService {

    // 增加員工
    Integer addEmployee(EmployeeCreateDto employeeCreateDto);

    // 修改員工狀態 By empId
    void toggleEmployeeStatus(int empId);

    // 修改員工權限
    void updateEmployeeRole(int empId, List<Integer> newRoleIds);

    // 更新員工資料，管理權限為 Admin 才可執行
    void updateEmployee(int empId, @Valid EmployeeUpdateDto employeeUpdateDto);

    // 條件查詢員工 (根據職等、模糊查詢、停權名單等)
    List<EmployeeProfileDto> getEmployees(EmployeeQueryParamsDto empQueryParams);

    // 查詢所有職等
    List<EmpRole> getAllRoles();

    // 查詢該員工所有權限
    List<EmpRole> getRolesById(int empId);

    // 查詢員工資料 By empId
    EmployeeProfileDto getEmployeeById(int empId);

    // 查詢員工資料 By email
    EmployeeProfileDto passEmpInfo(String email);
}
