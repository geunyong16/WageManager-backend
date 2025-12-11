package com.example.wagemanager.global.oauth.kakao.dto;

import lombok.Builder;

@Builder
public record KakaoUserInfo(
        String kakaoId,
        String name
) {
}
