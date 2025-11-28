package com.example.wagemanager.global.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserResponse {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String name;

        @JsonProperty("phone_number")
        private String phoneNumber;

        private KakaoProfile profile;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfile {
        private String nickname;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;
    }

    public KakaoUserInfo toUserInfo() {
        String kakaoIdValue = id != null ? String.valueOf(id) : null;
        String nameValue = null;
        String phoneValue = null;
        String profileImageValue = null;

        if (kakaoAccount != null) {
            nameValue = kakaoAccount.getName();
            phoneValue = kakaoAccount.getPhoneNumber();

            KakaoProfile profile = kakaoAccount.getProfile();
            if (profile != null) {
                profileImageValue = profile.getProfileImageUrl();
            }
        }

        return KakaoUserInfo.builder()
                .kakaoId(kakaoIdValue)
                .name(nameValue)
                .phoneNumber(phoneValue)
                .profileImageUrl(profileImageValue)
                .build();
    }
}
