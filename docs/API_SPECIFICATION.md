# WageManager API 명세서

## 목차
1. [인증 API](#1-인증-api)
2. [사용자 API](#2-사용자-api)
3. [고용주 API](#3-고용주-api)
4. [근로자 API](#4-근로자-api)
5. [공통 API](#5-공통-api)

---

## 1. 인증 API

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 1.1 | 카카오 로그인 | POST | `/api/auth/kakao/login` | 카카오 소셜 로그인 | 대기 | 대기 | - |
| 1.2 | 로그아웃 | POST | `/api/auth/logout` | 로그아웃 및 토큰 무효화 | 대기 | 대기 | - |
| 1.3 | 토큰 갱신 | POST | `/api/auth/refresh` | Access Token 갱신 | 대기 | 대기 | - |

### 1.1 카카오 로그인

**Request:**
```json
{
  "kakao_access_token": "kakao_access_token_here"
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "kakao_id": "kakao_12345",
      "name": "박성호",
      "phone": "010-1234-5678",
      "user_type": "EMPLOYER",
      "profile_image_url": "https://example.com/profile.jpg"
    }
  }
}
```

**Response (에러):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "유효하지 않은 카카오 토큰입니다."
  }
}
```

### 1.2 로그아웃

**Request:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (성공):**
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

### 1.3 토큰 갱신

**Request:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

## 2. 사용자 API

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 2.1 | 회원가입 | POST | `/api/users/register` | 신규 사용자 등록 (고용주/근로자) | 대기 | 대기 | - |
| 2.2 | 내 정보 조회 | GET | `/api/users/me` | 현재 로그인한 사용자 정보 조회 | 대기 | 대기 | - |
| 2.3 | 내 정보 수정 | PUT | `/api/users/me` | 사용자 정보 수정 | 대기 | 대기 | - |
| 2.4 | 계좌 정보 등록/수정 | PUT | `/api/users/me/account` | 근로자 계좌 정보 등록/수정 | 대기 | 대기 | - |

### 2.1 회원가입

**Request (고용주):**
```json
{
  "kakao_id": "kakao_12345",
  "name": "박성호",
  "phone": "010-1234-5678",
  "user_type": "EMPLOYER",
  "profile_image_url": "https://example.com/profile.jpg"
}
```

**Request (근로자):**
```json
{
  "kakao_id": "kakao_67890",
  "name": "김민지",
  "phone": "010-9876-5432",
  "user_type": "WORKER",
  "profile_image_url": "https://example.com/profile.jpg",
  "account": {
    "bank_name": "카카오뱅크",
    "account_number": "3333-12-1234567",
    "kakao_pay_link": "https://qr.kakaopay.com/abc123"
  }
}
```

**Response (성공 - 근로자):**
```json
{
  "success": true,
  "data": {
    "user_id": 2,
    "worker_id": 1,
    "worker_code": "ABC123",
    "name": "김민지",
    "phone": "010-9876-5432",
    "message": "회원가입이 완료되었습니다. 근로자 코드를 고용주에게 알려주세요."
  }
}
```

### 2.2 내 정보 조회

**Response (고용주):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "kakao_id": "kakao_12345",
    "name": "박성호",
    "phone": "010-1234-5678",
    "user_type": "EMPLOYER",
    "profile_image_url": "https://example.com/profile.jpg",
    "employer": {
      "id": 1,
      "phone": "010-1234-5678"
    }
  }
}
```

**Response (근로자):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "kakao_id": "kakao_67890",
    "name": "김민지",
    "phone": "010-9876-5432",
    "user_type": "WORKER",
    "profile_image_url": "https://example.com/profile.jpg",
    "worker": {
      "id": 1,
      "worker_code": "ABC123",
      "bank_name": "카카오뱅크",
      "account_number": "3333-12-****567",
      "kakao_pay_link": "https://qr.kakaopay.com/abc123"
    }
  }
}
```

### 2.3 내 정보 수정

**Request:**
```json
{
  "name": "박성호",
  "phone": "010-1234-5679",
  "profile_image_url": "https://example.com/new_profile.jpg"
}
```

**Response (성공):**
```json
{
  "success": true,
  "message": "정보가 수정되었습니다."
}
```

### 2.4 계좌 정보 등록/수정

**Request:**
```json
{
  "bank_name": "국민은행",
  "account_number": "1234-56-7890123",
  "kakao_pay_link": "https://qr.kakaopay.com/xyz789"
}
```

**Response (성공):**
```json
{
  "success": true,
  "message": "계좌 정보가 수정되었습니다."
}
```

---

## 3. 고용주 API

### 3.1 사업장 관리

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 3.1.1 | 사업장 등록 | POST | `/api/employer/workplaces` | 새 사업장 등록 | 대기 | 대기 | - |
| 3.1.2 | 사업장 목록 조회 | GET | `/api/employer/workplaces` | 내 사업장 목록 조회 | 대기 | 대기 | - |
| 3.1.3 | 사업장 상세 조회 | GET | `/api/employer/workplaces/{id}` | 특정 사업장 상세 정보 | 대기 | 대기 | - |
| 3.1.4 | 사업장 수정 | PUT | `/api/employer/workplaces/{id}` | 사업장 정보 수정 | 대기 | 대기 | - |
| 3.1.5 | 사업장 비활성화 | DELETE | `/api/employer/workplaces/{id}` | 사업장 비활성화 | 대기 | 대기 | - |

#### 3.1.1 사업장 등록

**Request:**
```json
{
  "business_number": "123-45-67890",
  "business_name": "(주)카페모카",
  "name": "홍대점",
  "address": "서울시 마포구 홍익로 123",
  "color_code": "#FF6B6B"
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "business_number": "123-45-67890",
    "business_name": "(주)카페모카",
    "name": "홍대점",
    "address": "서울시 마포구 홍익로 123",
    "color_code": "#FF6B6B",
    "is_active": true,
    "created_at": "2025-11-01T10:00:00Z"
  }
}
```

#### 3.1.2 사업장 목록 조회

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "business_name": "(주)카페모카",
      "name": "홍대점",
      "address": "서울시 마포구 홍익로 123",
      "color_code": "#FF6B6B",
      "is_active": true,
      "worker_count": 5
    },
    {
      "id": 2,
      "business_name": "(주)카페모카",
      "name": "강남점",
      "address": "서울시 강남구 테헤란로 456",
      "color_code": "#4ECDC4",
      "is_active": true,
      "worker_count": 3
    }
  ]
}
```

### 3.2 근로자 계약 관리

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 3.2.1 | 근로자 추가 | POST | `/api/employer/workplaces/{workplace_id}/workers` | 근로자 코드로 근로자 추가 및 계약 | 대기 | 대기 | - |
| 3.2.2 | 근로자 목록 조회 | GET | `/api/employer/workplaces/{workplace_id}/workers` | 사업장별 근로자 목록 | 대기 | 대기 | - |
| 3.2.3 | 계약 상세 조회 | GET | `/api/employer/contracts/{contract_id}` | 근로 계약 상세 정보 | 대기 | 대기 | - |
| 3.2.4 | 계약 수정 | PUT | `/api/employer/contracts/{contract_id}` | 근로 계약 정보 수정 | 대기 | 대기 | - |
| 3.2.5 | 계약 종료 | DELETE | `/api/employer/contracts/{contract_id}` | 근로 계약 종료 | 대기 | 대기 | - |

#### 3.2.1 근로자 추가

**Request:**
```json
{
  "worker_code": "ABC123",
  "hourly_wage": 10000,
  "work_days": [1, 2, 3, 4, 5],
  "contract_start_date": "2025-11-01",
  "contract_end_date": null,
  "payment_day": 25
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "contract_id": 1,
    "workplace": {
      "id": 1,
      "name": "홍대점"
    },
    "worker": {
      "id": 1,
      "name": "김민지",
      "worker_code": "ABC123"
    },
    "hourly_wage": 10000,
    "work_days": [1, 2, 3, 4, 5],
    "contract_start_date": "2025-11-01",
    "payment_day": 25,
    "is_active": true
  }
}
```

#### 3.2.2 근로자 목록 조회

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "contract_id": 1,
      "worker": {
        "id": 1,
        "name": "김민지",
        "worker_code": "ABC123",
        "phone": "010-9876-5432"
      },
      "hourly_wage": 10000,
      "work_days": [1, 2, 3, 4, 5],
      "contract_start_date": "2025-11-01",
      "payment_day": 25,
      "is_active": true
    }
  ]
}
```

### 3.3 근무 일정 관리

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 3.3.1 | 근무 일정 등록 | POST | `/api/employer/work-records` | 근무 일정 등록 (SCHEDULED) | 대기 | 대기 | - |
| 3.3.2 | 근무 일정 일괄 등록 | POST | `/api/employer/work-records/batch` | 여러 일정 한번에 등록 | 대기 | 대기 | - |
| 3.3.3 | 근무 기록 조회 | GET | `/api/employer/work-records` | 캘린더용 근무 기록 조회 | 대기 | 대기 | - |
| 3.3.4 | 근무 일정 수정 | PUT | `/api/employer/work-records/{id}` | 근무 시간 수정 | 대기 | 대기 | - |
| 3.3.5 | 근무 완료 처리 | PUT | `/api/employer/work-records/{id}/complete` | 근무 완료 상태로 변경 | 대기 | 대기 | - |
| 3.3.6 | 근무 일정 삭제 | DELETE | `/api/employer/work-records/{id}` | 근무 일정 삭제 | 대기 | 대기 | - |

#### 3.3.1 근무 일정 등록

**Request:**
```json
{
  "contract_id": 1,
  "work_date": "2025-11-01",
  "start_time": "09:00",
  "end_time": "14:00",
  "memo": null
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "contract_id": 1,
    "work_date": "2025-11-01",
    "start_time": "09:00",
    "end_time": "14:00",
    "total_hours": 0,
    "status": "SCHEDULED",
    "memo": null,
    "worker": {
      "id": 1,
      "name": "김민지"
    },
    "workplace": {
      "id": 1,
      "name": "홍대점",
      "color_code": "#FF6B6B"
    }
  }
}
```

#### 3.3.2 근무 일정 일괄 등록

**Request:**
```json
{
  "contract_id": 1,
  "work_records": [
    {
      "work_date": "2025-11-01",
      "start_time": "09:00",
      "end_time": "14:00"
    },
    {
      "work_date": "2025-11-02",
      "start_time": "09:00",
      "end_time": "14:00"
    }
  ]
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "created_count": 20,
    "message": "20개의 근무 일정이 등록되었습니다."
  }
}
```

#### 3.3.3 근무 기록 조회

**Query Parameters:**
- `workplace_id`: 사업장 ID (필수)
- `year`: 연도 (필수)
- `month`: 월 (필수)
- `status`: 상태 필터 (선택: SCHEDULED, MODIFIED_BEFORE, COMPLETED, MODIFIED_AFTER)

**Example:** `GET /api/employer/work-records?workplace_id=1&year=2025&month=11`

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "work_date": "2025-11-01",
      "start_time": "09:00",
      "end_time": "14:00",
      "total_hours": 5.0,
      "regular_hours": 5.0,
      "overtime_hours": 0,
      "night_hours": 0,
      "holiday_hours": 0,
      "status": "COMPLETED",
      "memo": null,
      "worker": {
        "id": 1,
        "name": "김민지"
      },
      "contract": {
        "id": 1,
        "hourly_wage": 10000
      }
    },
    {
      "id": 2,
      "work_date": "2025-11-02",
      "start_time": "09:00",
      "end_time": "14:00",
      "total_hours": 0,
      "status": "SCHEDULED",
      "memo": null,
      "worker": {
        "id": 1,
        "name": "김민지"
      }
    }
  ]
}
```

#### 3.3.4 근무 일정 수정

**Request (근무 전):**
```json
{
  "start_time": "10:00",
  "end_time": "15:00",
  "memo": "재고 정리 예정"
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "work_date": "2025-11-15",
    "start_time": "10:00",
    "end_time": "15:00",
    "status": "MODIFIED_BEFORE",
    "memo": "재고 정리 예정",
    "notification_sent": true
  }
}
```

#### 3.3.5 근무 완료 처리

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "work_date": "2025-11-01",
    "start_time": "09:00",
    "end_time": "14:00",
    "total_hours": 5.0,
    "regular_hours": 5.0,
    "status": "COMPLETED"
  }
}
```

### 3.4 정정 요청 관리

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 3.4.1 | 정정 요청 목록 | GET | `/api/employer/correction-requests` | 정정 요청 목록 조회 | 대기 | 대기 | - |
| 3.4.2 | 정정 요청 상세 | GET | `/api/employer/correction-requests/{id}` | 정정 요청 상세 조회 | 대기 | 대기 | - |
| 3.4.3 | 정정 요청 승인 | PUT | `/api/employer/correction-requests/{id}/approve` | 정정 요청 승인 | 대기 | 대기 | - |
| 3.4.4 | 정정 요청 반려 | PUT | `/api/employer/correction-requests/{id}/reject` | 정정 요청 반려 | 대기 | 대기 | - |

#### 3.4.1 정정 요청 목록

**Query Parameters:**
- `workplace_id`: 사업장 ID (선택)
- `status`: 상태 필터 (선택: PENDING, APPROVED, REJECTED)

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "work_record_id": 1,
      "requester": {
        "id": 2,
        "name": "김민지"
      },
      "workplace": {
        "id": 1,
        "name": "홍대점"
      },
      "requested_work_date": "2025-11-01",
      "current_start_time": "09:00",
      "current_end_time": "14:00",
      "requested_start_time": "09:00",
      "requested_end_time": "14:30",
      "reason": "손님이 많아서 30분 연장 근무했습니다",
      "status": "PENDING",
      "created_at": "2025-11-02T10:00:00Z"
    }
  ]
}
```

#### 3.4.3 정정 요청 승인

**Request:**
```json
{
  "review_comment": "확인했습니다"
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "APPROVED",
    "reviewed_at": "2025-11-02T15:00:00Z",
    "review_comment": "확인했습니다",
    "work_record_updated": true,
    "notification_sent": true
  }
}
```

### 3.5 급여 관리

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 3.5.1 | 급여 목록 조회 | GET | `/api/employer/salaries` | 월별 급여 목록 조회 | 대기 | 대기 | - |
| 3.5.2 | 급여 상세 조회 | GET | `/api/employer/salaries/{id}` | 급여 상세 정보 조회 | 대기 | 대기 | - |
| 3.5.3 | 급여 계산 | POST | `/api/employer/salaries/calculate` | 특정 월 급여 계산 | 대기 | 대기 | - |
| 3.5.4 | 급여 송금 | POST | `/api/employer/payments` | 급여 송금 처리 | 대기 | 대기 | - |
| 3.5.5 | 송금 보류 | PUT | `/api/employer/payments/{id}/hold` | 송금 보류 처리 | 대기 | 대기 | - |

#### 3.5.1 급여 목록 조회

**Query Parameters:**
- `workplace_id`: 사업장 ID (필수)
- `year`: 연도 (필수)
- `month`: 월 (필수)

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "salary_id": 1,
      "contract_id": 1,
      "worker": {
        "id": 1,
        "name": "김민지"
      },
      "year": 2025,
      "month": 11,
      "total_work_hours": 110,
      "base_pay": 1000000,
      "overtime_pay": 120000,
      "night_pay": 25000,
      "holiday_pay": 0,
      "total_gross_pay": 1145000,
      "four_major_insurance": 115000,
      "income_tax": 20000,
      "local_income_tax": 2000,
      "total_deduction": 137000,
      "net_pay": 1008000,
      "payment_due_date": "2025-11-25",
      "payment_status": "PENDING"
    }
  ]
}
```

#### 3.5.4 급여 송금

**Request:**
```json
{
  "salary_id": 1,
  "payment_method": "KAKAO_PAY"
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "payment_id": 1,
    "salary_id": 1,
    "payment_method": "KAKAO_PAY",
    "status": "COMPLETED",
    "payment_date": "2025-11-25T14:32:00Z",
    "transaction_id": "KP20251125ABC123",
    "kakao_pay_link": "https://qr.kakaopay.com/abc123"
  }
}
```

---

## 4. 근로자 API

### 4.1 근무 일정 조회

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 4.1.1 | 내 근무 일정 조회 | GET | `/api/worker/work-records` | 월별 근무 일정 및 기록 조회 | 대기 | 대기 | - |
| 4.1.2 | 근무 기록 상세 | GET | `/api/worker/work-records/{id}` | 특정 근무 기록 상세 조회 | 대기 | 대기 | - |
| 4.1.3 | 근무 완료 처리 | PUT | `/api/worker/work-records/{id}/complete` | 근무 완료 처리 | 대기 | 대기 | - |

#### 4.1.1 내 근무 일정 조회

**Query Parameters:**
- `year`: 연도 (필수)
- `month`: 월 (필수)
- `status`: 상태 필터 (선택)

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "work_date": "2025-11-01",
      "start_time": "09:00",
      "end_time": "14:00",
      "total_hours": 5.0,
      "status": "COMPLETED",
      "workplace": {
        "id": 1,
        "name": "홍대점",
        "color_code": "#FF6B6B"
      }
    },
    {
      "id": 2,
      "work_date": "2025-11-02",
      "start_time": "09:00",
      "end_time": "14:00",
      "total_hours": 0,
      "status": "SCHEDULED",
      "workplace": {
        "id": 1,
        "name": "홍대점",
        "color_code": "#FF6B6B"
      }
    }
  ]
}
```

### 4.2 정정 요청

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 4.2.1 | 정정 요청 생성 | POST | `/api/worker/correction-requests` | 근무 시간 정정 요청 | 대기 | 대기 | - |
| 4.2.2 | 내 정정 요청 조회 | GET | `/api/worker/correction-requests` | 내 정정 요청 목록 | 대기 | 대기 | - |
| 4.2.3 | 정정 요청 취소 | DELETE | `/api/worker/correction-requests/{id}` | 대기 중인 정정 요청 취소 | 대기 | 대기 | - |

#### 4.2.1 정정 요청 생성

**Request:**
```json
{
  "work_record_id": 1,
  "requested_work_date": "2025-11-01",
  "requested_start_time": "09:00",
  "requested_end_time": "14:30",
  "reason": "손님이 많아서 30분 연장 근무했습니다"
}
```

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "work_record_id": 1,
    "requested_work_date": "2025-11-01",
    "requested_start_time": "09:00",
    "requested_end_time": "14:30",
    "reason": "손님이 많아서 30분 연장 근무했습니다",
    "status": "PENDING",
    "created_at": "2025-11-02T10:00:00Z"
  }
}
```

### 4.3 급여 조회

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 4.3.1 | 내 급여 목록 | GET | `/api/worker/salaries` | 연도별 급여 목록 조회 | 대기 | 대기 | - |
| 4.3.2 | 급여 상세 조회 | GET | `/api/worker/salaries/{id}` | 급여 상세 정보 조회 | 대기 | 대기 | - |
| 4.3.3 | 송금 내역 조회 | GET | `/api/worker/payments` | 송금 내역 조회 | 대기 | 대기 | - |

#### 4.3.1 내 급여 목록

**Query Parameters:**
- `year`: 연도 (필수)

**Response (성공):**
```json
{
  "success": true,
  "data": [
    {
      "salary_id": 1,
      "workplace": {
        "id": 1,
        "name": "홍대점"
      },
      "year": 2025,
      "month": 11,
      "total_work_hours": 110,
      "total_gross_pay": 1145000,
      "total_deduction": 137000,
      "net_pay": 1008000,
      "payment_due_date": "2025-11-25",
      "payment_status": "COMPLETED",
      "payment_date": "2025-11-25T14:32:00Z"
    }
  ]
}
```

#### 4.3.2 급여 상세 조회

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "salary_id": 1,
    "workplace": {
      "id": 1,
      "name": "홍대점"
    },
    "year": 2025,
    "month": 11,
    "total_work_hours": 110,
    "base_pay": 1000000,
    "overtime_pay": 120000,
    "night_pay": 25000,
    "holiday_pay": 0,
    "total_gross_pay": 1145000,
    "four_major_insurance": 115000,
    "income_tax": 20000,
    "local_income_tax": 2000,
    "total_deduction": 137000,
    "net_pay": 1008000,
    "payment_due_date": "2025-11-25",
    "work_records": [
      {
        "work_date": "2025-11-01",
        "start_time": "09:00",
        "end_time": "14:30",
        "total_hours": 5.5,
        "regular_hours": 5.5,
        "overtime_hours": 0,
        "status": "MODIFIED_AFTER"
      }
    ]
  }
}
```

---

## 5. 공통 API

### 5.1 알림

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 5.1.1 | 알림 목록 조회 | GET | `/api/notifications` | 내 알림 목록 조회 | 대기 | 대기 | - |
| 5.1.2 | 알림 읽음 처리 | PUT | `/api/notifications/{id}/read` | 알림 읽음 처리 | 대기 | 대기 | - |
| 5.1.3 | 모든 알림 읽음 | PUT | `/api/notifications/read-all` | 모든 알림 읽음 처리 | 대기 | 대기 | - |
| 5.1.4 | 알림 삭제 | DELETE | `/api/notifications/{id}` | 알림 삭제 | 대기 | 대기 | - |

#### 5.1.1 알림 목록 조회

**Query Parameters:**
- `is_read`: 읽음 여부 필터 (선택: true/false)
- `type`: 알림 유형 필터 (선택)
- `page`: 페이지 번호 (기본값: 1)
- `limit`: 페이지당 개수 (기본값: 20)

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": 1,
        "type": "SCHEDULE_CHANGE",
        "title": "근무 일정 변경",
        "message": "11월 15일 근무 시간이 10:00~15:00로 변경되었습니다.",
        "link_url": "/work-records/2025/11",
        "is_read": false,
        "created_at": "2025-11-10T15:00:00Z"
      },
      {
        "id": 2,
        "type": "CORRECTION_RESPONSE",
        "title": "정정 요청 승인",
        "message": "11월 1일 근무 시간 정정 요청이 승인되었습니다.",
        "link_url": "/work-records/1",
        "is_read": true,
        "read_at": "2025-11-02T16:00:00Z",
        "created_at": "2025-11-02T15:00:00Z"
      }
    ],
    "pagination": {
      "current_page": 1,
      "total_pages": 3,
      "total_count": 45,
      "limit": 20
    },
    "unread_count": 12
  }
}
```

### 5.2 사용자 설정

| 인덱스 | 기능 | Method | API Path | 설명 | FE 개발 현황 | BE 개발 현황 | 수정일 |
|--------|------|--------|----------|------|--------------|--------------|--------|
| 5.2.1 | 설정 조회 | GET | `/api/settings` | 사용자 알림 설정 조회 | 대기 | 대기 | - |
| 5.2.2 | 설정 수정 | PUT | `/api/settings` | 사용자 알림 설정 수정 | 대기 | 대기 | - |

#### 5.2.1 설정 조회

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "notification_enabled": true,
    "push_enabled": true,
    "email_enabled": false,
    "sms_enabled": false,
    "schedule_change_alert_enabled": true,
    "payment_alert_enabled": true,
    "correction_request_alert_enabled": true
  }
}
```

#### 5.2.2 설정 수정

**Request:**
```json
{
  "notification_enabled": true,
  "push_enabled": true,
  "email_enabled": false,
  "sms_enabled": true,
  "schedule_change_alert_enabled": true,
  "payment_alert_enabled": true,
  "correction_request_alert_enabled": false
}
```

**Response (성공):**
```json
{
  "success": true,
  "message": "설정이 저장되었습니다."
}
```

---

## 공통 에러 응답 형식

모든 API는 에러 발생 시 다음 형식으로 응답합니다:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자에게 표시할 에러 메시지",
    "details": {
      "field": "에러가 발생한 필드",
      "value": "잘못된 값"
    }
  }
}
```

### 주요 에러 코드

| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `UNAUTHORIZED` | 401 | 인증되지 않은 요청 |
| `FORBIDDEN` | 403 | 권한이 없는 요청 |
| `NOT_FOUND` | 404 | 리소스를 찾을 수 없음 |
| `VALIDATION_ERROR` | 400 | 입력값 검증 실패 |
| `DUPLICATE_ENTRY` | 409 | 중복된 데이터 |
| `BUSINESS_NUMBER_INVALID` | 400 | 잘못된 사업자등록번호 |
| `WORKER_CODE_NOT_FOUND` | 404 | 근로자 코드를 찾을 수 없음 |
| `CONTRACT_ALREADY_EXISTS` | 409 | 이미 계약이 존재함 |
| `WORK_RECORD_NOT_EDITABLE` | 400 | 수정할 수 없는 근무 기록 |
| `INSUFFICIENT_BALANCE` | 400 | 잔액 부족 (송금 시) |
| `PAYMENT_FAILED` | 500 | 송금 실패 |

---

## API 인증

모든 API 요청에는 JWT 토큰을 포함해야 합니다 (인증 API 제외).

**Header:**
```
Authorization: Bearer {access_token}
```

토큰이 만료된 경우 `/api/auth/refresh` 엔드포인트를 사용하여 갱신하세요.

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 2025-11-01 | 1.0 | 초안 작성 | Claude |
