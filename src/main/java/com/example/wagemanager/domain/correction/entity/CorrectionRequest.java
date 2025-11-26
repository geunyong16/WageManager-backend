package com.example.wagemanager.domain.correction.entity;

import com.example.wagemanager.common.BaseEntity;
import com.example.wagemanager.domain.correction.enums.CorrectionStatus;
import com.example.wagemanager.domain.user.entity.User;
import com.example.wagemanager.domain.workrecord.entity.WorkRecord;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "correction_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CorrectionRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_record_id", nullable = false)
    private WorkRecord workRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // 원본 근무 시간 (정정요청 생성 시점의 값 저장)
    @Column(name = "original_work_date", nullable = false)
    private LocalDate originalWorkDate;

    @Column(name = "original_start_time", nullable = false)
    private LocalTime originalStartTime;

    @Column(name = "original_end_time", nullable = false)
    private LocalTime originalEndTime;

    // 요청 근무 시간
    @Column(name = "requested_work_date", nullable = false)
    private LocalDate requestedWorkDate;

    @Column(name = "requested_start_time", nullable = false)
    private LocalTime requestedStartTime;

    @Column(name = "requested_end_time", nullable = false)
    private LocalTime requestedEndTime;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CorrectionStatus status = CorrectionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    public void approve(User reviewer, String reviewComment) {
        this.status = CorrectionStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = reviewComment;

        // WorkRecord 업데이트
        this.workRecord.updateWorkTime(this.requestedStartTime, this.requestedEndTime, null);
    }

    public void reject(User reviewer, String reviewComment) {
        this.status = CorrectionStatus.REJECTED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = reviewComment;
    }
}
