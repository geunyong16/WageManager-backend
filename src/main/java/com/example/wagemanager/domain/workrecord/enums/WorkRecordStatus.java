package com.example.wagemanager.domain.workrecord.enums;

public enum WorkRecordStatus {
    SCHEDULED,          // 예정 (근무 전)
    MODIFIED_BEFORE,    // 수정 (근무 전)
    COMPLETED,          // 완료 (근무 후)
    MODIFIED_AFTER      // 수정 (근무 후)
}
