package com.ryanshiun.seniorscare.device.dao;

import com.ryanshiun.seniorscare.device.model.Address;
import java.util.List;

public interface AddressDao {

    //新增會員收件地址 包含 memberId、收件人、電話、地址等資料）
    void addAddress(Address address);

     // 更新會員收件地址（必須包含 id，用於辨識要修改哪一筆資料）
    void updateAddress(Address address);

    // 根據id刪除會員收件地址
    void deleteAddress(Integer id);

     //查詢指定會員的所有收件地址 by 會員 ID return 該會員的所有收件地址清單
    List<Address> getAddressesByMemberId(Integer memberId);

     //設定預設收件地址 會先將該會員所有地址的 is_default 重置為 0  再將指定的 addressId 設定為 1
    void setDefaultAddress(Integer memberId, Integer addressId);

}
