package com.example.wagemanager.domain.workrecord.scheduler;

import com.example.wagemanager.domain.contract.entity.WorkerContract;
import com.example.wagemanager.domain.workrecord.repository.WorkRecordRepository;
import com.example.wagemanager.domain.workrecord.service.WorkRecordGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkRecordScheduler {

    private final WorkRecordRepository workRecordRepository;
    private final WorkRecordGenerationService workRecordGenerationService;

    /**
     * 매월 15일 오전 2시에 2개월 뒤 WorkRecord 생성
     * 항상 2개월치 데이터를 유지하기 위해 2개월 뒤의 데이터를 생성
     * cron: 초 분 시 일 월 요일
     * "0 0 2 15 * *" = 매월 15일 오전 2시 0분 0초
     */
    @Scheduled(cron = "0 0 2 15 * *")
    @Transactional
    public void generateTwoMonthsLaterWorkRecords() {
        log.info("===== 2개월 뒤 WorkRecord 자동 생성 스케줄러 시작 =====");

        try {
            // 모든 활성 계약 조회
            List<WorkerContract> activeContracts = workRecordRepository.findAllActiveContracts();
            log.info("활성 계약 수: {}", activeContracts.size());

            int successCount = 0;
            int failCount = 0;

            // 각 계약마다 2개월 뒤 WorkRecord 생성
            for (WorkerContract contract : activeContracts) {
                try {
                    workRecordGenerationService.generateTwoMonthsLaterWorkRecords(contract);
                    successCount++;
                } catch (Exception e) {
                    log.error("WorkRecord 생성 실패: Contract ID={}, Error={}", contract.getId(), e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("===== 2개월 뒤 WorkRecord 자동 생성 완료 ===== (성공: {}, 실패: {})", successCount, failCount);
        } catch (Exception e) {
            log.error("WorkRecord 자동 생성 스케줄러 실행 중 오류 발생", e);
        }
    }
}
