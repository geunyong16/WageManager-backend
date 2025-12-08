package com.example.wagemanager.domain.salary.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 급여 공제 계산 유틸리티
 *
 * 대상자:
 * - 프리랜서: 소득세 3% + 지방소득세 0.3%
 * - 비정규직(알바): 세금/보험료 적용 여부에 따라 계산
 */
public class DeductionCalculator {

    /**
     * 급여 공제 유형
     * 프리랜서와 비정규직의 세금 및 4대보험 공제 방식을 통합 관리
     */
    public enum PayrollDeductionType {
        FREELANCER,                     // 프리랜서: 소득세 3% + 지방소득세 0.3%
        PART_TIME_NONE,                 // 비정규직: 세금 X, 4대보험 X
        PART_TIME_TAX_ONLY,             // 비정규직: 세금 O, 4대보험 X
        PART_TIME_TAX_AND_INSURANCE     // 비정규직: 세금 O, 4대보험 O
    }

    // 최소 국민연금 보험료 기준 월급 (39만원)
    private static final BigDecimal MINIMUM_PENSION_WAGE = new BigDecimal("390000");

    // 4대보험 요율
    private static final BigDecimal NATIONAL_PENSION_RATE = new BigDecimal("0.045");     // 국민연금 4.5%
    private static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.03545");  // 건강보험 3.545%
    private static final BigDecimal LONG_TERM_CARE_RATE = new BigDecimal("0.1295");    // 장기요양보험 (건강보험의 12.95%)
    private static final BigDecimal EMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.009"); // 고용보험 0.9%

    /**
     * 통합 세금 및 보험료 계산 결과
     * 프리랜서와 비정규직 모두 사용 가능한 통합 결과 클래스
     */
    public static class TaxResult {
        public BigDecimal nationalPension;     // 국민연금
        public BigDecimal healthInsurance;     // 건강보험
        public BigDecimal longTermCare;        // 장기요양보험
        public BigDecimal employmentInsurance; // 고용보험
        public BigDecimal totalInsurance;      // 총 보험료

        public BigDecimal incomeTax;           // 소득세
        public BigDecimal localIncomeTax;      // 지방소득세
        public BigDecimal totalTax;            // 총 세금

        public BigDecimal totalDeduction;      // 총 공제 (보험료 + 세금)

        public TaxResult(BigDecimal grossPay, PayrollDeductionType deductionType) {
            switch (deductionType) {
                case FREELANCER:
                    // 프리랜서: 소득세 3% + 지방소득세 0.3%
                    this.nationalPension = BigDecimal.ZERO;
                    this.healthInsurance = BigDecimal.ZERO;
                    this.longTermCare = BigDecimal.ZERO;
                    this.employmentInsurance = BigDecimal.ZERO;
                    this.totalInsurance = BigDecimal.ZERO;

                    this.incomeTax = grossPay.multiply(new BigDecimal("0.03"))
                        .setScale(0, RoundingMode.DOWN);
                    this.localIncomeTax = grossPay.multiply(new BigDecimal("0.003"))
                        .setScale(0, RoundingMode.DOWN);
                    this.totalTax = this.incomeTax.add(this.localIncomeTax);
                    this.totalDeduction = this.totalTax;
                    break;

                case PART_TIME_NONE:
                    // 비정규직: 세금 X, 4대보험 X
                    this.nationalPension = BigDecimal.ZERO;
                    this.healthInsurance = BigDecimal.ZERO;
                    this.longTermCare = BigDecimal.ZERO;
                    this.employmentInsurance = BigDecimal.ZERO;
                    this.totalInsurance = BigDecimal.ZERO;
                    this.incomeTax = BigDecimal.ZERO;
                    this.localIncomeTax = BigDecimal.ZERO;
                    this.totalTax = BigDecimal.ZERO;
                    this.totalDeduction = BigDecimal.ZERO;
                    break;

                case PART_TIME_TAX_ONLY:
                    // 비정규직: 세금 O, 4대보험 X
                    this.nationalPension = BigDecimal.ZERO;
                    this.healthInsurance = BigDecimal.ZERO;
                    this.longTermCare = BigDecimal.ZERO;
                    this.employmentInsurance = BigDecimal.ZERO;
                    this.totalInsurance = BigDecimal.ZERO;

                    this.incomeTax = calculateSimpleIncomeTax(grossPay)
                        .setScale(0, RoundingMode.DOWN);
                    this.localIncomeTax = this.incomeTax.multiply(new BigDecimal("0.1"))
                        .setScale(0, RoundingMode.DOWN);
                    this.totalTax = this.incomeTax.add(this.localIncomeTax);
                    this.totalDeduction = this.totalTax;
                    break;

                case PART_TIME_TAX_AND_INSURANCE:
                    // 비정규직: 세금 O, 4대보험 O
                    BigDecimal pensionBaseSalary = grossPay.compareTo(MINIMUM_PENSION_WAGE) < 0
                        ? MINIMUM_PENSION_WAGE
                        : grossPay;
                    pensionBaseSalary = pensionBaseSalary.setScale(-3, RoundingMode.DOWN);

                    this.nationalPension = pensionBaseSalary.multiply(NATIONAL_PENSION_RATE)
                        .setScale(0, RoundingMode.DOWN);
                    this.healthInsurance = grossPay.multiply(HEALTH_INSURANCE_RATE)
                        .setScale(0, RoundingMode.DOWN);
                    this.longTermCare = this.healthInsurance.multiply(LONG_TERM_CARE_RATE)
                        .setScale(0, RoundingMode.DOWN);
                    this.employmentInsurance = grossPay.multiply(EMPLOYMENT_INSURANCE_RATE)
                        .setScale(0, RoundingMode.DOWN);
                    this.totalInsurance = this.nationalPension.add(this.healthInsurance)
                        .add(this.longTermCare).add(this.employmentInsurance);

                    this.incomeTax = calculateSimpleIncomeTax(grossPay)
                        .setScale(0, RoundingMode.DOWN);
                    this.localIncomeTax = this.incomeTax.multiply(new BigDecimal("0.1"))
                        .setScale(0, RoundingMode.DOWN);
                    this.totalTax = this.incomeTax.add(this.localIncomeTax);
                    this.totalDeduction = this.totalInsurance.add(this.totalTax);
                    break;
            }
        }
    }

    /**
     * 통합 세금 및 보험료 계산
     */
    public static TaxResult calculate(BigDecimal grossPay, PayrollDeductionType deductionType) {
        return new TaxResult(grossPay, deductionType);
    }

    /**
     * 간단한 소득세 계산 (간이세액표 근사)
     * Todo: 실제 간이세액표를 반영한 정확한 계산 로직으로 대체 필요
     */
    private static BigDecimal calculateSimpleIncomeTax(BigDecimal grossPay) {
        // 매우 간단한 근사: 대략 3~5%
        // 실제 간이세액표를 사용하려면 더 복잡한 로직 필요
        if (grossPay.compareTo(new BigDecimal("1000000")) < 0) {
            return grossPay.multiply(new BigDecimal("0.03"));
        } else if (grossPay.compareTo(new BigDecimal("2000000")) < 0) {
            return grossPay.multiply(new BigDecimal("0.035"));
        } else {
            return grossPay.multiply(new BigDecimal("0.04"));
        }
    }
}
