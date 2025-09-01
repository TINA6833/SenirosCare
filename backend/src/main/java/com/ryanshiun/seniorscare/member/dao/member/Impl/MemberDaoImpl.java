package com.ryanshiun.seniorscare.member.dao.member.Impl;

import com.ryanshiun.seniorscare.member.dao.member.MemberDao;
import com.ryanshiun.seniorscare.member.dto.member.*;
import com.ryanshiun.seniorscare.member.model.member.Member;
import com.ryanshiun.seniorscare.member.rowmapper.member.MemberQueryMapper;
import com.ryanshiun.seniorscare.member.rowmapper.member.MemberRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MemberDaoImpl implements MemberDao {
    LocalDateTime now = LocalDateTime.now();

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    // 提取共用的 SQL 查詢會員語法
    private static final String BASE_MEMBER_QUERY =
        """
        SELECT
            member_id,
            member_name,
            main_phone,
            gender,
            birthday,
            address,
            email,
            image_path,
            is_active,
            ban_reason,
            created_at,
            updated_at,
            login_at
        FROM member
        """;

    /** 查詢會員資料 (前端用)
     * @param memberId 員工 ID
     * @return 會員詳細資料
     */
    @Override
    public MemberProfileDto getMemberById(Integer memberId) {
        final String sql = BASE_MEMBER_QUERY + " WHERE member_id = :memberId";

        Map<String, Object> map = new HashMap<>();
        map.put("memberId", memberId);
        List<MemberProfileDto> members = namedParameterJdbcTemplate.query(sql, map, new MemberQueryMapper());
        if (!members.isEmpty()) {
            return members.get(0);
        } else {
            return null;
        }
    }


    /** 查詢會員資料 (line登入用)
     * @param lineUserId line ID
     * @return 會員詳細資料
     */
    @Override
    public Member passMemberInfoByLineId(String lineUserId) {
        final String sql = "SELECT * FROM member WHERE line_user_id = :lineUserId";
        Map<String, Object> map = new HashMap<>();
        map.put("lineUserId", lineUserId);
        List<Member> members = namedParameterJdbcTemplate.query(sql, map, new MemberRowMapper());
        if (!members.isEmpty()) {
            return members.get(0);
        } else {
            return null;
        }
    }

    /** 查詢 email 是否已經被註冊 (登入用)
     * @param email 會員電子郵件
     * @return 會員 ID
     */
    @Override
    public Integer isEmailExists(String email) {
        final String sql = "SELECT member_id FROM member WHERE email = :email";
        Map<String, Object> map = new HashMap<>();
        return namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);
    }

    /** 客製化查詢會員 (姓名、地址、性別、是否停權)
     * @param memberQueryParams 查詢參數
     * @return 會員列表
     */
    @Override
    public List<MemberProfileDto> getMembers(MemberQueryParamsDto memberQueryParams) {
        StringBuilder sql = new StringBuilder(BASE_MEMBER_QUERY);
        sql.append(" WHERE 1=1 ");

        Map<String, Object> map = new HashMap<>();

        if (memberQueryParams.getMemberName() != null) {
            sql.append(" AND member_name LIKE :memberName ");
            map.put("memberName", "%" + memberQueryParams.getMemberName() + "%");
        }

        if (memberQueryParams.getAddress() != null) {
            sql.append(" AND address LIKE :address ");
            map.put("address", memberQueryParams.getAddress());
        }

        if (memberQueryParams.getGender() != null) {
            sql.append(" AND member_gender LIKE :gender ");
            map.put("gender", memberQueryParams.getGender());
        }

        if (memberQueryParams.getIsActive() != null) {
            sql.append(" AND is_active = :isActive ");
            map.put("isActive", memberQueryParams.getIsActive());
        }
        return namedParameterJdbcTemplate.query(sql.toString(), map, new MemberQueryMapper());
    }

    /** 修改個人資料
     * @param memberUpdateDto 更新資料
     */
    @Override
    public void updateProfile(MemberUpdateDto memberUpdateDto) {
        final String sql = """
            UPDATE member
            SET member_name = :memberName,
                main_phone = :mainPhone,
                gender = :gender,
                birthday = :birthday,
                email = :email,
                address = :address,
                updated_at = :updatedAt
            WHERE member_id = :memberId
            """;

        Map<String, Object> map = new HashMap<>();
        map.put("memberId", memberUpdateDto.getMemberId());
        map.put("memberName", memberUpdateDto.getMemberName());
        map.put("mainPhone", memberUpdateDto.getMainPhone());
        map.put("gender", memberUpdateDto.isGender());
        map.put("birthday", memberUpdateDto.getBirthday());
        map.put("address", memberUpdateDto.getAddress());
        map.put("email", memberUpdateDto.getEmail());
        map.put("updatedAt", now);
        namedParameterJdbcTemplate.update(sql, map);
    }

    /** 啟用或停權會員
     * @param banReasonDto 停權原因資料
     */
    @Override
    public void toggleMemberStatus(BanReasonDto banReasonDto) {
        final String sql = """
            UPDATE member
            SET is_active = :isActive,
                updated_at = :updatedAt,
                ban_reason = :banReason
            WHERE member_id = :memberId
            """;
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", banReasonDto.getMemberId());
        map.put("isActive", banReasonDto.getIsActive());
        map.put("banReason", banReasonDto.getBanReason());
        map.put("updatedAt", now);
        namedParameterJdbcTemplate.update(sql,map);
    }

    /* ------------------------- 登入相關 -------------------------- */

    /** 更新登入時間
     * @param lineUserId line ID
     */
    @Override
    public void updateLastLogin(String lineUserId) {
        final String sql = """
            UPDATE member
            SET login_at = :loginAt
            WHERE line_user_id = :lineUserId
            """;
        Map<String, Object> map = new HashMap<>();
        map.put("lineUserId", lineUserId);
        map.put("loginAt", now);
        namedParameterJdbcTemplate.update(sql, map);
    }

    /** 建立基本資料
     * @param memberRegisterDto 註冊資料
     * @return 新增的會員 ID
     */
    @Override
    public Integer register(MemberRegisterDto memberRegisterDto) {
        String sql = """
            INSERT INTO member (line_user_id, member_name, email, image_path)
            VALUES (:lineUserId ,:memberName, :email, :imagePath)
            """;
        Map<String, Object> map = new HashMap<>();
        map.put("lineUserId", memberRegisterDto.getLineUserId());
        map.put("memberName", memberRegisterDto.getMemberName());
        map.put("email", memberRegisterDto.getEmail());
        map.put("imagePath", memberRegisterDto.getImagePath());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

}
