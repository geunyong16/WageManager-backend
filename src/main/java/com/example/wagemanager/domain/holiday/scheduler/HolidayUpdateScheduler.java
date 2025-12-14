package com.example.wagemanager.domain.holiday.scheduler;

import com.example.wagemanager.domain.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 공휴일 정보 자동 업데이트 스케줄러
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HolidayUpdateScheduler {

    private final HolidayService holidayService;

    /**
     * 애플리케이션 시작 시 공휴일 정보 초기화
     * DB에 공휴일 정보가 없으면 자동으로 가져옴
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeHolidays() {
        log.info("공휴일 정보 초기화 시작");

        int currentYear = LocalDate.now().getYear();

        try {
            // 현재 연도 공휴일 정보 확인
            if (!holidayService.hasHolidays(currentYear)) {
                log.info("{}년 공휴일 정보가 없습니다. API에서 가져옵니다.", currentYear);
                holidayService.updateCurrentAndNextYearHolidays();
            } else {
                log.info("{}년 공휴일 정보가 이미 존재합니다. ({}개)",
                        currentYear, holidayService.countHolidays(currentYear));
            }

            // 다음 연도 공휴일 정보 확인
            int nextYear = currentYear + 1;
            if (!holidayService.hasHolidays(nextYear)) {
                log.info("{}년 공휴일 정보가 없습니다. API에서 가져옵니다.", nextYear);
                holidayService.updateHolidays(nextYear);
            }

        } catch (Exception e) {
            log.error("공휴일 정보 초기화 실패: {}", e.getMessage());
            log.warn("공휴일 정보 없이 서비스를 시작합니다. 토/일만 휴일로 인식됩니다.");
        }
    }

    /**
     * 매년 1월 1일 0시에 실행
     * 올해와 내년 공휴일 정보 업데이트
     */
    @Scheduled(cron = "0 0 0 1 1 *") // 매년 1월 1일 00:00:00
    public void updateYearlyHolidays() {
        log.info("연간 공휴일 업데이트 스케줄러 실행");

        try {
            int count = holidayService.updateCurrentAndNextYearHolidays();
            log.info("공휴일 정보 업데이트 완료: {}개", count);

        } catch (Exception e) {
            log.error("공휴일 업데이트 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 매월 1일 1시에 실행
     * 공휴일 정보 재확인 및 누락 체크
     */
    @Scheduled(cron = "0 0 1 1 * *") // 매월 1일 01:00:00
    public void checkMonthlyHolidays() {
        log.info("월간 공휴일 체크 스케줄러 실행");

        int currentYear = LocalDate.now().getYear();

        try {
            long count = holidayService.countHolidays(currentYear);
            log.info("{}년 공휴일 {}개 등록 확인", currentYear, count);

            // 공휴일이 하나도 없으면 재시도
            if (count == 0) {
                log.warn("공휴일 정보가 없습니다. 재업데이트를 시도합니다.");
                holidayService.updateCurrentAndNextYearHolidays();
            }

        } catch (Exception e) {
            log.error("공휴일 체크 실패: {}", e.getMessage(), e);
        }
    }
}
