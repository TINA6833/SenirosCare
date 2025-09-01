package com.ryanshiun.seniorscare.security;

import com.ryanshiun.seniorscare.member.dao.member.MemberDao;
import com.ryanshiun.seniorscare.member.dto.member.MemberRegisterDto;
import com.ryanshiun.seniorscare.member.model.member.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Component
public class LineMemberService extends DefaultOAuth2UserService {

    @Autowired
    private MemberDao memberDao;

    private static final Logger logger = LoggerFactory.getLogger(LineMemberService.class);

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        logger.info("LINE User Attributes Received: {}", attributes);

        // 取得 oAuth2User 的資訊
        String lineUserId = oAuth2User.getName(); // providerId
        String memberName = Objects.toString(attributes.get("displayName"));
        // 從 ID Token 中取得 email
        String email = Objects.toString(attributes.get("email"));
        String pictureUrl = Objects.toString(attributes.get("pictureUrl"));
        // 尚未用到
//        String accessToken = userRequest.getAccessToken().getTokenValue();
//        Date expiresAt = Date.from(userRequest.getAccessToken().getExpiresAt());

        // 檢查是否已經有會員資料
        Member member = memberDao.passMemberInfoByLineId(lineUserId);
        if (member == null) {
            // 如果沒有，則新增會員資料
            MemberRegisterDto memberRegisterDto = new MemberRegisterDto();
            memberRegisterDto.setLineUserId(lineUserId);
            memberRegisterDto.setMemberName(memberName);
            memberRegisterDto.setEmail(email);
            memberRegisterDto.setImagePath(pictureUrl);
            // 先註冊新會員
            memberDao.register(memberRegisterDto);

            // 重新查詢會員資料以獲取 memberId
            member = memberDao.passMemberInfoByLineId(lineUserId);
        }

        if (member != null && !member.getActive()) {
            // 如果帳號被停用，拋出 DisabledException
            // Spring Security 會捕獲這個例外，並將其轉交給 FailureHandler 處理
            throw new DisabledException("會員帳號已被停權: " + member.getMemberId());
        }
        return new CustomOAuth2User(oAuth2User, member.getMemberId());
    }
}
