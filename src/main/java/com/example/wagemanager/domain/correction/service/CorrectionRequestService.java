package com.example.wagemanager.domain.correction.service;

import com.example.wagemanager.domain.correction.dto.CorrectionRequestDto;
import com.example.wagemanager.domain.correction.entity.CorrectionRequest;
import com.example.wagemanager.domain.correction.enums.CorrectionStatus;
import com.example.wagemanager.domain.correction.repository.CorrectionRequestRepository;
import com.example.wagemanager.domain.user.entity.User;
import com.example.wagemanager.domain.workrecord.entity.WorkRecord;
import com.example.wagemanager.domain.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorrectionRequestService {

    private final CorrectionRequestRepository correctionRequestRepository;
    private final WorkRecordRepository workRecordRepository;

    // ===== 근로자용 API =====

    /**
     * 정정요청 생성
     */
    @Transactional
    public CorrectionRequestDto.Response createCorrectionRequest(User requester, CorrectionRequestDto.CreateRequest request) {
        WorkRecord workRecord = workRecordRepository.findById(request.getWorkRecordId())
                .orElseThrow(() -> new IllegalArgumentException("근무 기록을 찾을 수 없습니다."));

        // 해당 근무기록에 이미 대기중인 정정요청이 있는지 확인
        if (correctionRequestRepository.existsByWorkRecordIdAndStatus(request.getWorkRecordId(), CorrectionStatus.PENDING)) {
            throw new IllegalStateException("해당 근무 기록에 이미 대기중인 정정요청이 있습니다.");
        }

        // 본인의 근무기록인지 확인
        if (!workRecord.getContract().getWorker().getUser().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("본인의 근무 기록에 대해서만 정정요청을 할 수 있습니다.");
        }

        CorrectionRequest correctionRequest = CorrectionRequest.builder()
                .workRecord(workRecord)
                .requester(requester)
                // 원본 시간 저장 (생성 시점의 WorkRecord 값)
                .originalWorkDate(workRecord.getWorkDate())
                .originalStartTime(workRecord.getStartTime())
                .originalEndTime(workRecord.getEndTime())
                // 요청 시간
                .requestedWorkDate(request.getRequestedWorkDate())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .reason(request.getReason())
                .status(CorrectionStatus.PENDING)
                .build();

        CorrectionRequest savedRequest = correctionRequestRepository.save(correctionRequest);
        return CorrectionRequestDto.Response.from(savedRequest);
    }

    /**
     * 내 정정요청 목록 조회
     */
    public List<CorrectionRequestDto.ListResponse> getMyCorrectionRequests(User requester, CorrectionStatus status) {
        List<CorrectionRequest> requests;
        if (status != null) {
            requests = correctionRequestRepository.findByRequesterIdAndStatus(requester.getId(), status);
        } else {
            requests = correctionRequestRepository.findByRequesterId(requester.getId());
        }

        return requests.stream()
                .map(CorrectionRequestDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 내 정정요청 상세 조회
     */
    public CorrectionRequestDto.Response getMyCorrectionRequest(User requester, Long correctionRequestId) {
        CorrectionRequest correctionRequest = correctionRequestRepository.findByIdWithDetails(correctionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("정정요청을 찾을 수 없습니다."));

        // 본인의 정정요청인지 확인
        if (!correctionRequest.getRequester().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("본인의 정정요청만 조회할 수 있습니다.");
        }

        return CorrectionRequestDto.Response.from(correctionRequest);
    }

    /**
     * 정정요청 취소 (PENDING 상태만 가능)
     */
    @Transactional
    public void cancelCorrectionRequest(User requester, Long correctionRequestId) {
        CorrectionRequest correctionRequest = correctionRequestRepository.findById(correctionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("정정요청을 찾을 수 없습니다."));

        // 본인의 정정요청인지 확인
        if (!correctionRequest.getRequester().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("본인의 정정요청만 취소할 수 있습니다.");
        }

        // PENDING 상태만 취소 가능
        if (correctionRequest.getStatus() != CorrectionStatus.PENDING) {
            throw new IllegalStateException("대기중인 정정요청만 취소할 수 있습니다.");
        }

        correctionRequestRepository.delete(correctionRequest);
    }

    // ===== 고용주용 API =====

    /**
     * 사업장별 정정요청 목록 조회
     */
    public List<CorrectionRequestDto.ListResponse> getCorrectionRequestsByWorkplace(Long workplaceId, CorrectionStatus status) {
        List<CorrectionRequest> requests;
        if (status != null) {
            requests = correctionRequestRepository.findByWorkplaceIdAndStatus(workplaceId, status);
        } else {
            requests = correctionRequestRepository.findByWorkplaceId(workplaceId);
        }

        return requests.stream()
                .map(CorrectionRequestDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 정정요청 상세 조회 (고용주용)
     */
    public CorrectionRequestDto.Response getCorrectionRequestDetail(Long correctionRequestId) {
        CorrectionRequest correctionRequest = correctionRequestRepository.findByIdWithDetails(correctionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("정정요청을 찾을 수 없습니다."));

        return CorrectionRequestDto.Response.from(correctionRequest);
    }

    /**
     * 정정요청 승인
     */
    @Transactional
    public CorrectionRequestDto.Response approveCorrectionRequest(User reviewer, Long correctionRequestId, CorrectionRequestDto.ReviewRequest request) {
        CorrectionRequest correctionRequest = correctionRequestRepository.findByIdWithDetails(correctionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("정정요청을 찾을 수 없습니다."));

        // PENDING 상태만 승인 가능
        if (correctionRequest.getStatus() != CorrectionStatus.PENDING) {
            throw new IllegalStateException("대기중인 정정요청만 승인할 수 있습니다.");
        }

        correctionRequest.approve(reviewer, request.getReviewComment());

        return CorrectionRequestDto.Response.from(correctionRequest);
    }

    /**
     * 정정요청 거절
     */
    @Transactional
    public CorrectionRequestDto.Response rejectCorrectionRequest(User reviewer, Long correctionRequestId, CorrectionRequestDto.ReviewRequest request) {
        CorrectionRequest correctionRequest = correctionRequestRepository.findByIdWithDetails(correctionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("정정요청을 찾을 수 없습니다."));

        // PENDING 상태만 거절 가능
        if (correctionRequest.getStatus() != CorrectionStatus.PENDING) {
            throw new IllegalStateException("대기중인 정정요청만 거절할 수 있습니다.");
        }

        correctionRequest.reject(reviewer, request.getReviewComment());

        return CorrectionRequestDto.Response.from(correctionRequest);
    }
}
