package com.ryanshiun.seniorscare.device.controller;

import com.ryanshiun.seniorscare.device.model.Address;
import com.ryanshiun.seniorscare.device.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin // 開啟跨來源請求（前端可直接呼叫 API）
public class AddressController {

    @Autowired
    private AddressService addressService;

   //新增收件地址
    @PostMapping
    public void addAddress(@RequestBody Address address) {
        addressService.addAddress(address);
    }


     // 更新收件地址 by address id
    @PutMapping("/{id}")
    public void updateAddress(@PathVariable Integer id, @RequestBody Address address) {
        address.setId(id);
        addressService.updateAddress(address);
    }

    // 刪除收件地址
    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Integer id) {
        addressService.deleteAddress(id);
    }


     //查詢指定會員的所有收件地址
    @GetMapping("/member")
    public List<Address> getAddressesByMemberId(Authentication authentication) {
        Integer mid = Integer.parseInt(authentication.getName());
        return addressService.getAddressesByMemberId(mid);
    }

     //設定會員的預設收件地址
    @PutMapping("/default")
    public void setDefaultAddress(@RequestParam Integer memberId, @RequestParam Integer addressId) {
        addressService.setDefaultAddress(memberId, addressId);
    }
}
