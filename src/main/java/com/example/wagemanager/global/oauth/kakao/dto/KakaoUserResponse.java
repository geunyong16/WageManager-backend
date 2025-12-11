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
    }

    public KakaoUserInfo toUserInfo() {
        String kakaoIdValue = id != null ? String.valueOf(id) : null;
        String nameValue = null;

        if (kakaoAccount != null) {
            nameValue = kakaoAccount.getName();
        }

        return KakaoUserInfo.builder()
                .kakaoId(kakaoIdValue)
                .name(nameValue)
                .build();
    }
}
