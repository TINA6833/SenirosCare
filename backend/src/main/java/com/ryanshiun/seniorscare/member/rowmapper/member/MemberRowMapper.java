package com.ryanshiun.seniorscare.member.rowmapper.member;

import com.ryanshiun.seniorscare.member.model.member.Member;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberRowMapper implements RowMapper<Member> {
    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member();

        member.setMemberId(rs.getInt("member_id"));
        member.setLineUserId(rs.getString("line_user_id"));
        member.setMemberName(rs.getString("member_name"));
        member.setMainPhone(rs.getString("main_phone"));
        member.setGender(rs.getBoolean("gender"));
        member.setBirthday(rs.getDate("birthday"));
        member.setAddress(rs.getString("address"));
        member.setEmail(rs.getString("email"));
        member.setActive(rs.getBoolean("is_active"));
        member.setBanReason(rs.getString("ban_reason"));
        member.setCreatedAt(rs.getTimestamp("created_at"));
        member.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        member.setLoginAt(rs.getTimestamp("login_at").toLocalDateTime());
        return member;
    }
}
