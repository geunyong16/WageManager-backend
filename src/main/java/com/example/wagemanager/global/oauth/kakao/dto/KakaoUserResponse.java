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
        private KakaoProfile profile;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfile {
        private String nickname;
    }

    public KakaoUserInfo toUserInfo() {
        String kakaoIdValue = String.valueOf(id);
        String nameValue = kakaoAccount.getProfile().getNickname();

        return KakaoUserInfo.builder()
                .kakaoId(kakaoIdValue)
                .name(nameValue)
                .build();
    }
}
