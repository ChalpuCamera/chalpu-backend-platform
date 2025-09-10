package com.example.chalpuplatform.oauth.security.oauth2.user;

import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        // Apple은 사용자 이름을 Identity Token에 포함하지 않음
        // 필요시 이메일 앞부분을 사용하거나 기본값 설정
        String email = getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Apple User";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        // Apple은 프로필 이미지를 제공하지 않음
        return null;
    }
}
