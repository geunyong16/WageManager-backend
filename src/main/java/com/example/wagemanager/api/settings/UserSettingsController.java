package com.example.wagemanager.api.settings;

import com.example.wagemanager.common.dto.ApiResponse;
import com.example.wagemanager.domain.settings.dto.UserSettingsDto;
import com.example.wagemanager.domain.settings.service.UserSettingsService;
import com.example.wagemanager.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 설정", description = "사용자 알림 설정 관리 API")
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @Operation(summary = "내 설정 조회", description = "로그인한 사용자 본인의 알림 설정을 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserSettingsDto.Response> getMySettings(@AuthenticationPrincipal User user) {
        return ApiResponse.success(userSettingsService.getUserSettings(user.getId()));
    }

    @Operation(summary = "내 설정 수정", description = "로그인한 사용자 본인의 알림 설정을 수정합니다.")
    @PutMapping("/me")
    public ApiResponse<UserSettingsDto.Response> updateMySettings(
            @AuthenticationPrincipal User user,
            @RequestBody UserSettingsDto.UpdateRequest request) {
        return ApiResponse.success(userSettingsService.updateUserSettings(user.getId(), request));
    }
}
