package com.example.wagemanager.domain.workrecord.service;

import com.example.wagemanager.common.exception.BadRequestException;
import com.example.wagemanager.common.exception.ErrorCode;
import com.example.wagemanager.common.exception.NotFoundException;
import com.example.wagemanager.domain.allowance.entity.WeeklyAllowance;
import com.example.wagemanager.domain.allowance.service.WeeklyAllowanceService;
import com.example.wagemanager.domain.contract.entity.WorkerContract;
import com.example.wagemanager.domain.contract.repository.WorkerContractRepository;
import com.example.wagemanager.domain.worker.entity.Worker;
import com.example.wagemanager.domain.worker.repository.WorkerRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkRecordService {

    private final WorkRecordRepository workRecordRepository;
    private final WorkerContractRepository workerContractRepository;
    private final WorkerRepository workerRepository;
    private final WeeklyAllowanceService weeklyAllowanceService;

    public List<WorkRecordDto.Response> getWorkRecordsByContract(Long contractId) {
        return workRecordRepository.findByContractId(contractId).stream()
                .map(WorkRecordDto.Response::from)
                .collect(Collectors.toList());
    }

    public WorkRecordDto.DetailedResponse getWorkRecordById(Long workRecordId) {
        WorkRecord workRecord = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORK_RECORD_NOT_FOUND, "근무 기록을 찾을 수 없습니다."));
        return WorkRecordDto.DetailedResponse.from(workRecord);
    }

    // 고용주용: 사업장의 근무 기록 조회 (캘린더)
    public List<WorkRecordDto.CalendarResponse> getWorkRecordsByWorkplaceAndDateRange(
            Long workplaceId, LocalDate startDate, LocalDate endDate) {
        List<WorkRecord> records = workRecordRepository.findByWorkplaceAndDateRange(workplaceId, startDate, endDate);
        return records.stream()
                .map(WorkRecordDto.CalendarResponse::from)
                .collect(Collectors.toList());
    }

    // 근로자용: 내 근무 기록 조회
    public List<WorkRecordDto.DetailedResponse> getWorkRecordsByWorkerAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        Worker worker = workerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORKER_NOT_FOUND, "근로자 정보를 찾을 수 없습니다."));

        List<WorkRecord> records = workRecordRepository.findByWorkerAndDateRange(worker.getId(), startDate, endDate);
        return records.stream()
                .map(WorkRecordDto.DetailedResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkRecordDto.Response createWorkRecord(WorkRecordDto.CreateRequest request) {
        WorkerContract contract = workerContractRepository.findById(request.getContractId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONTRACT_NOT_FOUND, "계약을 찾을 수 없습니다."));

        int totalMinutes = calculateWorkMinutes(
                LocalDateTime.of(request.getWorkDate(), request.getStartTime()),
                LocalDateTime.of(request.getWorkDate(), request.getEndTime()),
                request.getBreakMinutes() != null ? request.getBreakMinutes() : 0
        );

        // WorkRecord가 생성된 주에 WeeklyAllowance 자동 생성/조회
        WeeklyAllowance weeklyAllowance = weeklyAllowanceService.getOrCreateWeeklyAllowanceForDate(
                contract.getId(), request.getWorkDate());

        WorkRecord workRecord = WorkRecord.builder()
                .contract(contract)
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .breakMinutes(request.getBreakMinutes() != null ? request.getBreakMinutes() : 0)
                .totalWorkMinutes(totalMinutes)
                .status(WorkRecordStatus.SCHEDULED)
                .memo(request.getMemo())
                .weeklyAllowance(weeklyAllowance)
                .build();

        WorkRecord savedRecord = workRecordRepository.save(workRecord);

        // 양방향 관계 동기화
        savedRecord.addToWeeklyAllowance();

        // WeeklyAllowance의 수당 재계산
        weeklyAllowanceService.recalculateAllowances(weeklyAllowance.getId());

        return WorkRecordDto.Response.from(savedRecord);
    }

    @Transactional
    public List<WorkRecordDto.Response> batchCreateWorkRecords(WorkRecordDto.BatchCreateRequest request) {
        WorkerContract contract = workerContractRepository.findById(request.getContractId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONTRACT_NOT_FOUND, "계약을 찾을 수 없습니다."));

        int totalMinutes = calculateWorkMinutes(
                LocalDateTime.of(LocalDate.now(), request.getStartTime()),
                LocalDateTime.of(LocalDate.now(), request.getEndTime()),
                request.getBreakMinutes() != null ? request.getBreakMinutes() : 0
        );

        List<WorkRecord> workRecords = new ArrayList<>();
        for (LocalDate workDate : request.getWorkDates()) {
            // 각 WorkRecord가 생성될 주에 WeeklyAllowance 자동 생성/조회
            WeeklyAllowance weeklyAllowance = weeklyAllowanceService.getOrCreateWeeklyAllowanceForDate(
                    contract.getId(), workDate);

            WorkRecord workRecord = WorkRecord.builder()
                    .contract(contract)
                    .workDate(workDate)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .breakMinutes(request.getBreakMinutes() != null ? request.getBreakMinutes() : 0)
                    .totalWorkMinutes(totalMinutes)
                    .status(WorkRecordStatus.SCHEDULED)
                    .memo(request.getMemo())
                    .weeklyAllowance(weeklyAllowance)
                    .build();
            workRecords.add(workRecord);
        }

        List<WorkRecord> savedRecords = workRecordRepository.saveAll(workRecords);

        // 양방향 관계 동기화
        savedRecords.forEach(WorkRecord::addToWeeklyAllowance);

        // 각 주의 WeeklyAllowance 수당 재계산
        savedRecords.stream()
                .map(WorkRecord::getWeeklyAllowance)
                .distinct()
                .forEach(allowance -> weeklyAllowanceService.recalculateAllowances(allowance.getId()));

        return savedRecords.stream()
                .map(WorkRecordDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
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
            WeeklyAllowance newWeeklyAllowance = weeklyAllowanceService.getOrCreateWeeklyAllowanceForDate(
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

            // 기존 WeeklyAllowance 수당 재계산 (다른 WeeklyAllowance였다면)
            if (oldWeeklyAllowance != null && !oldWeeklyAllowance.getId().equals(newWeeklyAllowance.getId())) {
                weeklyAllowanceService.recalculateAllowances(oldWeeklyAllowance.getId());
            }

            // 새로운 WeeklyAllowance 수당 재계산
            weeklyAllowanceService.recalculateAllowances(newWeeklyAllowance.getId());
        }

        return WorkRecordDto.Response.from(workRecord);
    }

    @Transactional
    public void completeWorkRecord(Long workRecordId) {
        WorkRecord workRecord = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORK_RECORD_NOT_FOUND, "근무 기록을 찾을 수 없습니다."));
        workRecord.complete();
    }

    @Transactional
    public void deleteWorkRecord(Long workRecordId) {
        WorkRecord workRecord = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WORK_RECORD_NOT_FOUND, "근무 기록을 찾을 수 없습니다."));

        // SCHEDULED 상태만 삭제 가능
        if (workRecord.getStatus() != WorkRecordStatus.SCHEDULED) {
            throw new BadRequestException(ErrorCode.INVALID_WORK_RECORD_STATUS, "예정된 근무만 삭제할 수 있습니다.");
        }

        WeeklyAllowance weeklyAllowance = workRecord.getWeeklyAllowance();

        // 양방향 관계 해제
        workRecord.removeFromWeeklyAllowance();

        workRecordRepository.delete(workRecord);

        // WeeklyAllowance가 비어있으면 삭제
        if (weeklyAllowance != null) {
            // 양방향 관계가 이미 해제되었으므로 컬렉션만 확인
            if (weeklyAllowance.getWorkRecords().isEmpty()) {
                // WorkRecord가 없으면 WeeklyAllowance 삭제
                weeklyAllowanceService.deleteWeeklyAllowance(weeklyAllowance.getId());
            } else {
                // WorkRecord가 남아있으면 수당 재계산
                weeklyAllowanceService.recalculateAllowances(weeklyAllowance.getId());
            }
        }
    }

    private int calculateWorkMinutes(LocalDateTime start, LocalDateTime end, int breakMinutes) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        return (int) (totalMinutes - breakMinutes);
    }
}
