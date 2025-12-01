package com.example.wagemanager.domain.allowance.entity;

import com.example.wagemanager.common.BaseEntity;
import com.example.wagemanager.domain.contract.entity.WorkerContract;
import com.example.wagemanager.domain.workrecord.entity.WorkRecord;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_allowance",
        indexes = {
                @Index(name = "idx_contract_id", columnList = "contract_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyAllowance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private WorkerContract contract;

    // 해당 주의 근무 기록들
    @OneToMany(mappedBy = "weeklyAllowance", fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkRecord> workRecords = new ArrayList<>();

    // 주간 총 근무 시간
    @Column(name = "total_work_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalWorkHours = BigDecimal.ZERO;

    /**
     * 주휴수당 금액
     * 주 15시간 이상 근무 시 (1주 소정근로 시간 / 40) × 8 × 시급으로 계산됨
     */
    @Column(name = "weekly_paid_leave_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal weeklyPaidLeaveAmount = BigDecimal.ZERO;

    /**
     * 연장근로 시간
     * 주 40시간을 초과한 근무 시간
     */
    @Column(name = "overtime_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    /**
     * 연장수당 금액
     * 초과 시간 × 기본시급 × 1.5배율로 계산됨
     * overtimeHours와 함께 관리되어 조회 성능 최적화
     */
    @Column(name = "overtime_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    // 주간 총 근무 시간 계산 (WorkRecord 기반)
    public void calculateTotalWorkHours() {
        this.totalWorkHours = this.workRecords.stream()
                .map(WorkRecord::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 주휴수당 계산
    public void calculateWeeklyPaidLeave() {
        // 주 15시간 이상 근무 시 주휴수당 지급
        BigDecimal minimumHoursForPaidLeave = BigDecimal.valueOf(15);

        if (this.totalWorkHours.compareTo(minimumHoursForPaidLeave) >= 0) {
            // 주휴수당 = (1주 소정근로 시간 / 40) × 8 × 시급
            BigDecimal hourlyWage = this.contract.getHourlyWage();
            BigDecimal standardWorkHoursPerWeek = BigDecimal.valueOf(40);
            BigDecimal paidLeaveHours = BigDecimal.valueOf(8);

            this.weeklyPaidLeaveAmount = (this.totalWorkHours.divide(standardWorkHoursPerWeek, 2, java.math.RoundingMode.HALF_UP))
                    .multiply(paidLeaveHours)
                    .multiply(hourlyWage);
        } else {
            this.weeklyPaidLeaveAmount = BigDecimal.ZERO;
        }
    }

    // 연장수당 계산
    public void calculateOvertime() {
        // 주 40시간 초과 시 연장수당 지급
        BigDecimal standardWorkHoursPerWeek = BigDecimal.valueOf(40);
        BigDecimal overtimeRate = BigDecimal.valueOf(1.5); // 시급의 150%

        if (this.totalWorkHours.compareTo(standardWorkHoursPerWeek) > 0) {
            BigDecimal overtimeHoursCalculated = this.totalWorkHours.subtract(standardWorkHoursPerWeek);
            this.overtimeHours = overtimeHoursCalculated;

            // 연장수당 = 초과 시간 × (기본시급 × 1.5)
            BigDecimal hourlyWage = this.contract.getHourlyWage();
            this.overtimeAmount = overtimeHoursCalculated.multiply(hourlyWage).multiply(overtimeRate);
        } else {
            this.overtimeHours = BigDecimal.ZERO;
            this.overtimeAmount = BigDecimal.ZERO;
        }
    }

}
