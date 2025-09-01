package com.ryanshiun.seniorscare.member.rowmapper.member;

import com.ryanshiun.seniorscare.member.dto.member.MemberProfileDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberQueryMapper implements RowMapper<MemberProfileDto> {
    @Override
    public MemberProfileDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        MemberProfileDto member = new MemberProfileDto();

        member.setMemberId(rs.getInt("member_id"));
        member.setMemberName(rs.getString("member_name"));
        member.setMainPhone(rs.getString("main_phone"));
        member.setGender(rs.getBoolean("gender"));
        member.setBirthday(rs.getDate("birthday"));
        member.setAddress(rs.getString("address"));
        member.setEmail(rs.getString("email"));
        member.setImagePath(rs.getString("image_path"));
        member.setActive(rs.getBoolean("is_active"));
        member.setBanReason(rs.getString("ban_reason"));
        member.setCreatedAt(rs.getTimestamp("created_at"));
        member.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        member.setLoginAt(rs.getTimestamp("login_at").toLocalDateTime());
        return member;
    }
}
