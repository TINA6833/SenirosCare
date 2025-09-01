package com.ryanshiun.seniorscare.device.service;

import com.ryanshiun.seniorscare.device.model.Address;

import java.util.List;

public interface AddressService {
     //新增會員收件地址
    void addAddress(Address address);

     // 更新會員收件地址
    void updateAddress(Address address);

     // 刪除會員收件地址
    void deleteAddress(Integer id);

     // 查詢指定會員的所有收件地址
    List<Address> getAddressesByMemberId(Integer memberId);

     //設定預設收件地址
    void setDefaultAddress(Integer memberId, Integer addressId);
}

