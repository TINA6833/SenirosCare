package com.ryanshiun.seniorscare.device.service.Impl;

import com.ryanshiun.seniorscare.device.dao.AddressDao;
import com.ryanshiun.seniorscare.device.model.Address;
import com.ryanshiun.seniorscare.device.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressDao addressDao;


     //新增一筆地址資料。
    @Override
    public void addAddress(Address address) {
        addressDao.addAddress(address);
    }


     // 更新一筆地址資料。
    @Override
    public void updateAddress(Address address) {
        addressDao.updateAddress(address);
    }


     // 根據 ID 刪除一筆地址資料。
    @Override
    public void deleteAddress(Integer id) {
        addressDao.deleteAddress(id);
    }


     //根據會員 ID 取得所有對應的地址。
    @Override
    public List<Address> getAddressesByMemberId(Integer memberId) {
        return addressDao.getAddressesByMemberId(memberId);
    }

    //設定某地址為該會員的預設地址
    @Override
    public void setDefaultAddress(Integer memberId, Integer addressId) {
        addressDao.setDefaultAddress(memberId, addressId);
    }
}
