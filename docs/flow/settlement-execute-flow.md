# Settlement Execute API 흐름

## 1. 개요

본 문서는 SettlementExecute API의 실행 흐름과 트랜잭션 경계, 상태 전이 과정을 설명한다.

API : `POST` `/api/v1/settlements`

---

## 2. 전체 흐름 요약

```
Payment (COMPLETED)
        ↓
정산 실행 요청
        ↓
Settlement 생성 (READY)
        ↓
IN_PROGRESS
        ↓
정산 정책 계산
        ↓
SettlementItem 생성
        ↓
COMPLETED
        ↓
Payment.settledAt 설정
```

---

## 3. 상세 처리 단계

### step 1. Payment 조회
- request.paymentId 기준 조회
- 존재하지 않으면 예외 발생

### step 2. 정산 가능 여부 검증
- payment.isPayable() 조건
  - Payment.status == COMPLETED
  - Payment.amount > 0
  - Payment.settledAt == null

조건 불만족 시 예외 발생

### step 3. Settlement 생성
- Settlement 생성
- 초기 상태 : READY
- type : NORMAL
- totalAmount = payment.amount

제약
  - 이미 Settlement 존재시 예외
  - DB unique(payment_id)로 1:1 보장

### step 4. 상태 전이 : READY -> IN_PROGRESS
- Settlement.changeStatus(IN_PROGRESS)

### step 5. 정산 정책 계산
- SettlementPolicyService.calculate() 실행
  - 분배 금액 계산
  - SettlementItemResult 목록 생성
  - 금액 정합성 검증
- 예외 발생 가능성
  - INVALID_PARTY_SIZE
  - INVALID_DISTRIBUTION_AMOUNT
  - INTERNAL_SERVER_ERROR

### step 6. SettlementItem 생성
- SettlementItemRepository.saveAll()
  - 각 수령자별 금액 저장

### step 7. 정산 완료 처리
- Settlement.changeStatus(COMPLETED)
  - completedAt 기록
- Payment.settle()
  - settledAt 기록

---

## 4. 실패 처리 흐름
- try-catch 블록 내 예외 발생 시
  - SettlementFailureService.markFailed() 호출
  - 별도 트랜잭션(REQUIRES_NEW)에서 Settlement 상태를 FAILED로 변경
  - 예외 재던짐
- 결과
  - Settlement는 FAILED 상태로 남음
  - Payment.settledAt은 변경되지 않음

---

## 5. 트랜잭션 경계
- SettlementExecuteBusiness는 @Transactional
  - 성공 시 전체 커밋
  - 실패 시 롤백
- 단, FAILED상태 기록은 REQUIRES_NEW로 별도 커밋됨

---

## 6. 현재 구조 특징
- Payment 1건당 Settlement 1건 (1:1 구조)
- 부분 정산 미지원
- 재정산 미지원
- ADJUSTMENT 타입 미구현
- Settlement는 상태 머신 기반

---

## 7. 설계 의도
- 결제와 정산 책임 분리
- Settlement는 정산 실행 상태만 관리
- Payment는 정산 여부(settledAt)만 기록
- 정산 이력은 삭제하지 않음