package com.example.wagemanager.domain.allowance.repository;

import com.example.wagemanager.domain.allowance.entity.WeeklyAllowance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyAllowanceRepository extends JpaRepository<WeeklyAllowance, Long> {

    List<WeeklyAllowance> findByContractId(Long contractId);

    /**
     * 특정 날짜가 속한 주(월요일~일요일)의 WeeklyAllowance 조회
     * 같은 주에 이미 생성된 WeeklyAllowance가 있으면 반환
     */
    @Query("""
            SELECT wa FROM WeeklyAllowance wa
            WHERE wa.contract.id = :contractId
            AND FUNCTION('YEAR', wa.createdAt) = FUNCTION('YEAR', :targetDate)
            AND FUNCTION('WEEK', wa.createdAt) = FUNCTION('WEEK', :targetDate)
            LIMIT 1
            """)
    Optional<WeeklyAllowance> findByContractAndWeek(@Param("contractId") Long contractId, @Param("targetDate") LocalDate targetDate);
}
