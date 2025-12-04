package com.example.wagemanager.api.correctionrequest;

import com.example.wagemanager.common.dto.ApiResponse;
import com.example.wagemanager.domain.correction.dto.CorrectionRequestDto;
import com.example.wagemanager.domain.correction.enums.CorrectionStatus;
import com.example.wagemanager.domain.correction.service.CorrectionRequestService;
import com.example.wagemanager.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
public class EmployerCorrectionRequestController {

    private final CorrectionRequestService correctionRequestService;

    /**
     * 사업장별 정정요청 목록 조회
     * GET /api/employer/workplaces/{workplaceId}/correction-requests
     */
    @GetMapping("/workplaces/{workplaceId}/correction-requests")
    public ApiResponse<List<CorrectionRequestDto.ListResponse>> getCorrectionRequests(
            @PathVariable Long workplaceId,
            @RequestParam(required = false) CorrectionStatus status) {
        return ApiResponse.success(
                correctionRequestService.getCorrectionRequestsByWorkplace(workplaceId, status));
    }

    /**
     * 정정요청 상세 조회
     * GET /api/employer/correction-requests/{id}
     */
    @GetMapping("/correction-requests/{id}")
    public ApiResponse<CorrectionRequestDto.Response> getCorrectionRequest(@PathVariable Long id) {
        return ApiResponse.success(correctionRequestService.getCorrectionRequestDetail(id));
    }

    /**
     * 정정요청 승인
     * PUT /api/employer/correction-requests/{id}/approve
     */
    @PutMapping("/correction-requests/{id}/approve")
    public ApiResponse<CorrectionRequestDto.Response> approveCorrectionRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody(required = false) CorrectionRequestDto.ReviewRequest request) {
        CorrectionRequestDto.ReviewRequest reviewRequest = request != null ? request : new CorrectionRequestDto.ReviewRequest();
        return ApiResponse.success(
                correctionRequestService.approveCorrectionRequest(user, id, reviewRequest));
    }

    /**
     * 정정요청 거절
     * PUT /api/employer/correction-requests/{id}/reject
     */
    @PutMapping("/correction-requests/{id}/reject")
    public ApiResponse<CorrectionRequestDto.Response> rejectCorrectionRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody(required = false) CorrectionRequestDto.ReviewRequest request) {
        CorrectionRequestDto.ReviewRequest reviewRequest = request != null ? request : new CorrectionRequestDto.ReviewRequest();
        return ApiResponse.success(
                correctionRequestService.rejectCorrectionRequest(user, id, reviewRequest));
    }
}
