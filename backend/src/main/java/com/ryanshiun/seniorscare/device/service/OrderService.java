
package com.ryanshiun.seniorscare.device.service;

import com.ryanshiun.seniorscare.device.dto.*;
import com.ryanshiun.seniorscare.device.model.Order;

import java.util.List;

import com.ryanshiun.seniorscare.device.dto.GuestCheckoutRequest;



public interface OrderService {


     // 建立新訂單
     String createOrder(OrderRequest req);

     //根據訂單 ID 查詢單筆訂單
     Order getOrderById(Integer id);

     //查詢指定會員的所有訂單
     List<Order> getOrdersByMember(Integer memberId);

     // 列出所有訂單，並可依狀態篩選
     List<Order> listAll(String status);

    //更新訂單狀態
    void updateStatus(Integer id, StatusUpdateRequest req);

    //更新訂單付款資訊
    void updatePayment(Integer id, PaymentUpdateRequest req);

    //付款可用COD或 line pay
    void updatePaymentMethod(Integer id, PaymentMethodUpdateRequest req);

    //刪除訂單
    void delete(Integer id);




}
