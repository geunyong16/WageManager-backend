package com.example.wagemanager.api.correctionrequest;

import com.example.wagemanager.common.dto.ApiResponse;
import com.example.wagemanager.domain.correction.dto.CorrectionRequestDto;
import com.example.wagemanager.domain.correction.enums.CorrectionStatus;
import com.example.wagemanager.domain.correction.service.CorrectionRequestService;
import com.example.wagemanager.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker/correction-requests")
@RequiredArgsConstructor
public class WorkerCorrectionRequestController {

    private final CorrectionRequestService correctionRequestService;

    /**
     * 정정요청 생성
     * POST /api/worker/correction-requests
     */
    @PostMapping
    public ApiResponse<CorrectionRequestDto.Response> createCorrectionRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CorrectionRequestDto.CreateRequest request) {
        return ApiResponse.success(
                correctionRequestService.createCorrectionRequest(user, request));
    }

    /**
     * 내 정정요청 목록 조회
     * GET /api/worker/correction-requests
     */
    @GetMapping
    public ApiResponse<List<CorrectionRequestDto.ListResponse>> getMyCorrectionRequests(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) CorrectionStatus status) {
        return ApiResponse.success(
                correctionRequestService.getMyCorrectionRequests(user, status));
    }

    /**
     * 내 정정요청 상세 조회
     * GET /api/worker/correction-requests/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<CorrectionRequestDto.Response> getMyCorrectionRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ApiResponse.success(
                correctionRequestService.getMyCorrectionRequest(user, id));
    }

    /**
     * 정정요청 취소
     * DELETE /api/worker/correction-requests/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelCorrectionRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        correctionRequestService.cancelCorrectionRequest(user, id);
        return ApiResponse.success(null);
    }
}
