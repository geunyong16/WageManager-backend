package com.example.wagemanager.api.workrecord;

import com.example.wagemanager.common.dto.ApiResponse;
import com.example.wagemanager.domain.workrecord.dto.WorkRecordDto;
import com.example.wagemanager.domain.workrecord.service.WorkRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employer/work-records")
@RequiredArgsConstructor
public class EmployerWorkRecordController {

    private final WorkRecordService workRecordService;

    // 근무 일정 등록
    @PostMapping
    public ApiResponse<WorkRecordDto.Response> createWorkRecord(
            @Valid @RequestBody WorkRecordDto.CreateRequest request) {
        return ApiResponse.success(workRecordService.createWorkRecord(request));
    }

    // 근무 일정 일괄 등록
    @PostMapping("/batch")
    public ApiResponse<List<WorkRecordDto.Response>> batchCreateWorkRecords(
            @Valid @RequestBody WorkRecordDto.BatchCreateRequest request) {
        return ApiResponse.success(workRecordService.batchCreateWorkRecords(request));
    }

    // 근무 기록 조회 (캘린더)
    @GetMapping
    public ApiResponse<List<WorkRecordDto.CalendarResponse>> getWorkRecords(
            @RequestParam Long workplaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(
                workRecordService.getWorkRecordsByWorkplaceAndDateRange(workplaceId, startDate, endDate));
    }

    // 근무 기록 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<WorkRecordDto.DetailedResponse> getWorkRecord(@PathVariable Long id) {
        return ApiResponse.success(workRecordService.getWorkRecordById(id));
    }

    // 근무 일정 수정
    @PutMapping("/{id}")
    public ApiResponse<WorkRecordDto.Response> updateWorkRecord(
            @PathVariable Long id,
            @RequestBody WorkRecordDto.UpdateRequest request) {
        return ApiResponse.success(workRecordService.updateWorkRecord(id, request));
    }

    // 근무 완료 처리
    @PutMapping("/{id}/complete")
    public ApiResponse<Void> completeWorkRecord(@PathVariable Long id) {
        workRecordService.completeWorkRecord(id);
        return ApiResponse.success(null);
    }

    // 근무 일정 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkRecord(@PathVariable Long id) {
        workRecordService.deleteWorkRecord(id);
        return ApiResponse.success(null);
    }
}
