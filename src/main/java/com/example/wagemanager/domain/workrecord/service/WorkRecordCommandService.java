package com.example.wagemanager.domain.workrecord.service;

import com.example.wagemanager.common.exception.BadRequestException;
import com.example.wagemanager.common.exception.ErrorCode;
import com.example.wagemanager.common.exception.NotFoundException;
import com.example.wagemanager.domain.allowance.entity.WeeklyAllowance;
import com.example.wagemanager.domain.contract.entity.WorkerContract;
import com.example.wagemanager.domain.contract.repository.WorkerContractRepository;
import com.example.wagemanager.domain.workrecord.dto.WorkRecordDto;
import com.example.wagemanager.domain.workrecord.entity.WorkRecord;
import com.example.wagemanager.domain.workrecord.enums.WorkRecordStatus;
import com.example.wagemanager.domain.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkRecordCommandService {

    private final WorkRecordRepository workRecordRepository;
    private final WorkerContractRepository workerContractRepository;
    private final WorkRecordCoordinatorService coordinatorService;

    public WorkRecordDto.Response createWorkRecord(WorkRecordDto.CreateRequest request) {
        WorkerContract contract = workerContractRepository.findById(request.getContractId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONTRACT_NOT_FOUND, "계약을 찾을 수 없습니다."));

        int totalMinutes = calculateWorkMinutes(
                LocalDateTime.of(request.getWorkDate(), request.getStartTime()),
                LocalDateTime.of(request.getWorkDate(), request.getEndTime()),
                request.getBreakMinutes() != null ? request.getBreakMinutes() : 0
        );

        // 근무 날짜와 현재 날짜를 비교하여 상태 결정
        // 과거 날짜면 COMPLETED, 미래 날짜면 SCHEDULED
        WorkRecordStatus status = request.getWorkDate().isBefore(LocalDate.now())
                ? WorkRecordStatus.COMPLETED
                : WorkRecordStatus.SCHEDULED;

        // WorkRecord가 생성된 주에 WeeklyAllowance 자동 생성/조회
        WeeklyAllowance weeklyAllowance = coordinatorService.getOrCreateWeeklyAllowance(
                contract.getId(), request.getWorkDate());

        WorkRecord workRecord = WorkRecord.builder()
                .contract(contract)
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .breakMinutes(request.getBreakMinutes() != null ? request.getBreakMinutes() : 0)
                .totalWorkMinutes(totalMinutes)
                .status(status)
                .memo(request.getMemo())
                .weeklyAllowance(weeklyAllowance)
                .build();

        WorkRecord savedRecord = workRecordRepository.save(workRecord);

        // 도메인 간 협력 처리
        if (status == WorkRecordStatus.COMPLETED) {
            // COMPLETED로 생성된 경우 급여 재계산 포함
            coordinatorService.handleWorkRecordCreation(savedRecord);
            coordinatorService.handleWorkRecordCompletion(savedRecord);
        } else {
            // SCHEDULED로 생성된 경우 WeeklyAllowance만 재계산
            coordinatorService.handleWorkRecordCreation(savedRecord);
        }

        return WorkRecordDto.Response.from(savedRecord);
    }

    public WorkRecordDto.Response updateWorkRecord(Long workRecordId, WorkRecordDto.UpdateRequest request) {
        WorkRecord workRecord = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORK_RECORD_NOT_FOUND, "근무 기록을 찾을 수 없습니다."));

        if (request.getStartTime() != null || request.getEndTime() != null || request.getBreakMinutes() != null) {
            int totalMinutes = calculateWorkMinutes(
                    LocalDateTime.of(workRecord.getWorkDate(),
                            request.getStartTime() != null ? request.getStartTime() : workRecord.getStartTime()),
                    LocalDateTime.of(workRecord.getWorkDate(),
                            request.getEndTime() != null ? request.getEndTime() : workRecord.getEndTime()),
                    request.getBreakMinutes() != null ? request.getBreakMinutes() : workRecord.getBreakMinutes()
            );

            // 기존 WeeklyAllowance 저장 (나중에 재계산용)
            WeeklyAllowance oldWeeklyAllowance = workRecord.getWeeklyAllowance();

            // 기존 WeeklyAllowance에서 제거 (양방향 관계 해제)
            if (oldWeeklyAllowance != null) {
                workRecord.removeFromWeeklyAllowance();
            }

            // 현재 주에 맞는 WeeklyAllowance 조회/생성
            WeeklyAllowance newWeeklyAllowance = coordinatorService.getOrCreateWeeklyAllowance(
                    workRecord.getContract().getId(), workRecord.getWorkDate());

            // 새로운 WeeklyAllowance에 할당 (양방향 관계 설정)
            workRecord.assignToWeeklyAllowance(newWeeklyAllowance);
            workRecord.addToWeeklyAllowance();

            // WorkRecord 업데이트
            workRecord.updateWorkRecord(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getBreakMinutes(),
                    totalMinutes,
                    request.getMemo()
            );

            workRecordRepository.save(workRecord);

            // 도메인 간 협력 처리
            coordinatorService.handleWorkRecordUpdate(workRecord, oldWeeklyAllowance, newWeeklyAllowance);
        }

        return WorkRecordDto.Response.from(workRecord);
    }

    public void completeWorkRecord(Long workRecordId) {
        WorkRecord workRecord = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORK_RECORD_NOT_FOUND, "근무 기록을 찾을 수 없습니다."));
        workRecord.complete();

        // 근무 완료 시 급여 재계산 (COMPLETED 상태가 되어야 급여에 포함됨)
        coordinatorService.handleWorkRecordCompletion(workRecord);
    }

    public void deleteWorkRecord(Long workRecordId) {
        WorkRecord workRecord = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORK_RECORD_NOT_FOUND, "근무 기록을 찾을 수 없습니다."));

        // SCHEDULED, COMPLETED 모두 삭제 가능
        WorkRecordStatus status = workRecord.getStatus();
        WeeklyAllowance weeklyAllowance = workRecord.getWeeklyAllowance();

        // 양방향 관계 해제
        workRecord.removeFromWeeklyAllowance();

        workRecordRepository.delete(workRecord);

        // 도메인 간 협력 처리
        coordinatorService.handleWorkRecordDeletion(weeklyAllowance, workRecord, status);
    }

    private int calculateWorkMinutes(LocalDateTime start, LocalDateTime end, int breakMinutes) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        return (int) (totalMinutes - breakMinutes);
    }
}
