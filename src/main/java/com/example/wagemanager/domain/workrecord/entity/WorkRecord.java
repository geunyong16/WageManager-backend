package com.example.wagemanager.domain.workrecord.entity;

import com.example.wagemanager.common.BaseEntity;
import com.example.wagemanager.domain.contract.entity.WorkerContract;
import com.example.wagemanager.domain.workrecord.enums.WorkRecordStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "work_record",
        indexes = {
                @Index(name = "idx_contract_date_status", columnList = "contract_id,work_date,status")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private WorkerContract contract;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "total_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(name = "regular_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal regularHours = BigDecimal.ZERO;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "night_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal nightHours = BigDecimal.ZERO;

    @Column(name = "holiday_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal holidayHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WorkRecordStatus status = WorkRecordStatus.SCHEDULED;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    // 근무 전 수정
    public void updateBeforeWork(LocalTime startTime, LocalTime endTime, String memo) {
        this.startTime = startTime;
        this.endTime = endTime;
        if (memo != null) this.memo = memo;

        if (this.status == WorkRecordStatus.SCHEDULED) {
            this.status = WorkRecordStatus.MODIFIED_BEFORE;
        }
    }

    // 근무 완료
    public void complete() {
        this.status = WorkRecordStatus.COMPLETED;
        calculateHours();
    }

    // 근무 후 수정 (정정 요청 승인 시)
    public void updateAfterWork(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = WorkRecordStatus.MODIFIED_AFTER;
        calculateHours();
    }

    // 근무 시간 계산
    private void calculateHours() {
        // 간단한 계산 (실제로는 더 복잡한 로직 필요)
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        this.totalHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);

        // 일반 근무 시간 (8시간 이내)
        if (this.totalHours.compareTo(BigDecimal.valueOf(8)) <= 0) {
            this.regularHours = this.totalHours;
            this.overtimeHours = BigDecimal.ZERO;
        } else {
            this.regularHours = BigDecimal.valueOf(8);
            this.overtimeHours = this.totalHours.subtract(BigDecimal.valueOf(8));
        }

        // 야간 근무 (22시~06시) - 추후 구현
        this.nightHours = BigDecimal.ZERO;

        // 휴일 근무 - 추후 구현
        this.holidayHours = BigDecimal.ZERO;
    }
}
