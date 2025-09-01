package com.ryanshiun.seniorscare.member.controller.employee;

import com.ryanshiun.seniorscare.member.dto.employee.*;
import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import com.ryanshiun.seniorscare.member.service.employee.EmployeeService;
import com.ryanshiun.seniorscare.member.service.employee.PwdResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PwdResetService pwdResetService;

    /** 查詢全部員工
        * @param isActive 員工狀態
        * @param empName 員工姓名
        * 根據 isActive 和 empName 查詢員工資料
        * 如果 isActive 為 true，則查詢啟用的員工；如果為 false，則查詢停用的員工
        * 如果 empName 不為 null，則根據姓名模糊查詢員工資料，為 null，則查詢全部
     */
    @GetMapping
    public ResponseEntity<List<EmployeeProfileDto>> getEmployees(
            // 查詢條件
            @RequestParam(defaultValue = "true") Boolean isActive,
            @RequestParam(required = false) String empName) {
        // 創建一個 class (dto) 用於接收前端傳來的值
        EmployeeQueryParamsDto empQueryParams = new EmployeeQueryParamsDto();
        empQueryParams.setEmpName(empName);
        empQueryParams.setIsActive(isActive);

        List<EmployeeProfileDto> employees = employeeService.getEmployees(empQueryParams);
        return ResponseEntity.status(HttpStatus.OK).body(employees);
    }

    /**
     * 查詢該員工所有權限
     * @param empId 員工 ID
     * @return 員工權限資料，若不存在則回傳 404
     */
    @GetMapping("/{empId}/roles")
    public ResponseEntity<List<EmpRole>> getRolesById(@PathVariable Integer empId) {
        List<EmpRole> roleList = employeeService.getRolesById(empId);
        if (roleList != null) {
            return ResponseEntity.status(HttpStatus.OK).body(roleList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 查詢該員工資料詳細資料
     * @param empId 員工 ID
     * @return 員工資料，若不存在則回傳 404
     */
    @GetMapping("/{empId}")
    public ResponseEntity<EmployeeProfileDto> getEmployeeById(@PathVariable Integer empId) {
        EmployeeProfileDto employee = employeeService.getEmployeeById(empId);
        if (employee != null) {
            return ResponseEntity.status(HttpStatus.OK).body(employee);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** 查詢所有職等
     * @return 回傳全部職位
     */
    @GetMapping("/roles")
    public ResponseEntity<List<EmpRole>> getAllRoles() {
        List<EmpRole> roles = employeeService.getAllRoles();
        if (roles != null) {
            return ResponseEntity.status(HttpStatus.OK).body(roles);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 註冊新員工
     * @param employeeCreateDto 建立員工所需資料
     * @return 建立後的員工資料，回傳 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeProfileDto> addEmployee(@RequestBody @Valid EmployeeCreateDto employeeCreateDto) {
        // 取得新員工的 id
        Integer employeeId = employeeService.addEmployee(employeeCreateDto);

        // 將新增的員工回傳
        EmployeeProfileDto employee = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    /**
     * 更新指定員工資料
     *
     * @param empId       員工 ID
     * @param employeeUpdateDto 更新員工資料 DTO
     * @return 回傳更新後的員工資料，若不存在則回傳 404
     */
    @PutMapping("/{empId}")
    public ResponseEntity<EmployeeProfileDto> updateEmployee(
            @PathVariable Integer empId,
            @RequestBody @Valid EmployeeUpdateDto employeeUpdateDto
    ) {
        // 先檢查該員工使否存在
        EmployeeProfileDto employee = employeeService.getEmployeeById(empId);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        employeeService.updateEmployee(empId, employeeUpdateDto);


        EmployeeProfileDto updateEmployee = employeeService.getEmployeeById(empId);
        return ResponseEntity.status(HttpStatus.OK).body(updateEmployee);
    }

    /** 啟用 or 停用員工
     * 切換員工的啟用狀態
     * @param empId 員工 ID
     * @return 更新後的員工資料，若不存在則回傳 404
     */
    @PutMapping("/{empId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeProfileDto> toggleEmployeeStatus(
            @PathVariable Integer empId
    ) {
        employeeService.toggleEmployeeStatus(empId);

        // 將切換結果回傳
        EmployeeProfileDto employee = employeeService.getEmployeeById(empId);
        return ResponseEntity.status(HttpStatus.OK).body(employee);
    }


    /**
     * 重新賦予員工權限
     * @param empId 員工 ID
     * @param newRoleIds 新的職等 ID 列表
     * @return 回傳該員工新權限，若不存在則回傳 404
     */
    @PutMapping("/{empId}/roles")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmpRole>> updateEmployeeRole(
            @PathVariable Integer empId,
            @RequestBody List<Integer> newRoleIds
    ) {
        // 先檢查該員工使否存在
        EmployeeProfileDto employee = employeeService.getEmployeeById(empId);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 重新賦予員工職等
        employeeService.updateEmployeeRole(empId, newRoleIds);

        List<EmpRole> newRoleList = employeeService.getRolesById(empId);
        return ResponseEntity.status(HttpStatus.OK).body(newRoleList);
    }

    /** 直接修改密碼
     * @param empId 員工 ID
     * @param password 新密碼
     */
    @PutMapping("/reset/{empId}")
    public ResponseEntity<?> resetPassword(@PathVariable Integer empId,
                                           @RequestBody @Valid String password) {
        try {
            pwdResetService.changePassword(empId, password);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (RuntimeException e) {
            // 這裡可以根據不同的錯誤類型返回不同的狀態碼
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
