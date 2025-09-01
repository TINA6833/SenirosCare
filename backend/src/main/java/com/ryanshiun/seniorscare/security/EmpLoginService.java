package com.ryanshiun.seniorscare.security;

import com.ryanshiun.seniorscare.member.dao.employee.EmployeeDao;
import com.ryanshiun.seniorscare.member.model.employee.EmpRole;
import com.ryanshiun.seniorscare.member.model.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Component
public class EmpLoginService implements UserDetailsService {

    @Autowired
    private EmployeeDao employeeDao;
    /**
     * 根據 email 查詢員工登入資料
     * @param email 員工 email
     * @return UserDetails 包含員工資訊和權限
     * @throws UsernameNotFoundException 如果找不到員工
     * @throws DisabledException 如果員工已被停權
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 查詢員工登入資料
        Employee employee = employeeDao.getEmployeeByEmail(email);

        if (employee == null) {
            throw new UsernameNotFoundException("Employee not found with email: " + email);
        } else if (!employee.isActive()) {
            // 丟提示給前端告訴使用者已經被停權
            throw new DisabledException("Employee is inactive with email: " + email);
        } else {
            Integer empId = employee.getEmpId();
            String empPwd = employee.getPassword();

            List<EmpRole> empRoleList = employeeDao.getEmployeeRoles(empId);
            List<GrantedAuthority> authorities = convertToAuthorities(empRoleList);
            // 建立 UserDetails 物件
            return new User(String.valueOf(empId), empPwd, authorities);
        }

    }

    // 權限格式轉換
    private List<GrantedAuthority> convertToAuthorities(List<EmpRole> roleList) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (EmpRole role : roleList) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        return authorities;
    }
}
