package com.ryanshiun.seniorscare.device.dao.Impl;

import com.ryanshiun.seniorscare.device.dao.AddressDao;
import com.ryanshiun.seniorscare.device.model.Address;
import com.ryanshiun.seniorscare.device.rowmapper.AddressRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class AddressDaoImpl implements AddressDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;


     // 新增會員收件地址
    @Override
    public void addAddress(Address address) {
        String sql = "INSERT INTO address (member_id, recipient, phone, postal_code, address_line1, address_line2, is_default) " +
                "VALUES (:memberId, :recipient, :phone, :postalCode, :addressLine1, :addressLine2, :isDefault)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", address.getMemberId())
                .addValue("recipient", address.getRecipient())
                .addValue("phone", address.getPhone())
                .addValue("postalCode", address.getPostalCode())
                .addValue("addressLine1", address.getAddressLine1())
                .addValue("addressLine2", address.getAddressLine2())
                .addValue("isDefault", address.getIsDefault());

        jdbcTemplate.update(sql, params);
    }


     //更新會員收件地址
    @Override
    public void updateAddress(Address address) {
        String sql = "UPDATE address SET recipient = :recipient, phone = :phone, postal_code = :postalCode, " +
                "address_line1 = :addressLine1, address_line2 = :addressLine2, is_default = :isDefault " +
                "WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", address.getId())
                .addValue("recipient", address.getRecipient())
                .addValue("phone", address.getPhone())
                .addValue("postalCode", address.getPostalCode())
                .addValue("addressLine1", address.getAddressLine1())
                .addValue("addressLine2", address.getAddressLine2())
                .addValue("isDefault", address.getIsDefault());


        jdbcTemplate.update(sql, params);
    }

     //刪除會員收件地址 by  id
    @Override
    public void deleteAddress(Integer id) {
        String sql = "DELETE FROM address WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

     //查詢指定會員的所有收件地址 by  id
    @Override
    public List<Address> getAddressesByMemberId(Integer memberId) {
        String sql = "SELECT * FROM address WHERE member_id = :memberId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("memberId", memberId), new AddressRowMapper());
    }

   //設定會員預設收件地址
    @Override
    public void setDefaultAddress(Integer memberId, Integer addressId) {
        // 將該會員所有地址的 is_default 重置為 0
        String resetSql = "UPDATE address SET is_default = 0 WHERE member_id = :memberId";
        jdbcTemplate.update(resetSql, new MapSqlParameterSource("memberId", memberId));

        // 將指定的地址 ID 設為預設（is_default = 1）
        String setSql = "UPDATE address SET is_default = 1 WHERE id = :addressId AND member_id = :memberId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("addressId", addressId)
                .addValue("memberId", memberId);

        jdbcTemplate.update(setSql, params);
    }
}
