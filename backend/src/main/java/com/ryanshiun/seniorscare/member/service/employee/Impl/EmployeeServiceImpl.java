package com.ryanshiun.seniorscare.member.service.employee.Impl;

import com.ryanshiun.seniorscare.member.dao.employee.EmployeeDao;
import com.ryanshiun.seniorscare.member.dto.employee.*;
import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import com.ryanshiun.seniorscare.member.service.employee.EmpLogService;
import com.ryanshiun.seniorscare.member.service.employee.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private EmpLogService empLogService;

    // 增加員工
    @Transactional
    @Override
    public Integer addEmployee(EmployeeCreateDto employeeCreateDto) {
        Integer newEmpId = employeeDao.addEmployee(employeeCreateDto);
        employeeDao.assignInitialRole(newEmpId, employeeCreateDto.getRoleId());
        return newEmpId;
    }

    // 切換員工使用狀態
    @Override
    public void toggleEmployeeStatus(int empId) {
        EmployeeProfileDto employee = employeeDao.getEmployeeById(empId);
        boolean newStatus = !employee.getIsActive();
        employeeDao.toggleEmployeeStatus(empId, newStatus);
    }

    // 修改員工資料
    @Override
    public void updateEmployee(int empId, EmployeeUpdateDto employeeUpdateDto) {
        employeeDao.updateEmployee(empId, employeeUpdateDto);
    }

    /** 修改員工權限
     * 先刪除原有的權限，再新增新的權限
     * @param empId 員工 ID
     * @param newRoleIds 新的權限 ID 列表
     */
    @Transactional
    @Override
    public void updateEmployeeRole(int empId, List<Integer> newRoleIds) {
        employeeDao.deleteEmployeeRoles(empId);
        employeeDao.addEmployeeRole(empId, newRoleIds);
    }

    // 客製化搜尋
    @Override
    public List<EmployeeProfileDto> getEmployees(EmployeeQueryParamsDto empQueryParams) {
        return employeeDao.getEmployees(empQueryParams);
    }

    // 取得所有職等
    @Override
    public List<EmpRole> getAllRoles() {
        return employeeDao.getAllRoles();
    }

    // 查詢該員工所有權限
    @Override
    public List<EmpRole> getRolesById(int empId) {
        return employeeDao.getEmployeeRoles(empId);
    }

    // 查詢單一員工資料
    @Override
    public EmployeeProfileDto getEmployeeById(int empId) {
        return employeeDao.getEmployeeById(empId);
    }

    // 根據 email 查詢員工資料
    @Override
    public EmployeeProfileDto passEmpInfo(String email) {
        return employeeDao.passEmpInfo(email);
    }
}
