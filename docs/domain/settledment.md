# 정산(Settlement) 도메인 사양

## 1. 목적

Settlement는 결제 완료된 Payment를 기준으로 정산 금액을 계산하고 기록하는 도메인이다.

Settlement는 정산 실행 과정의 상태를 관리하며, 정산 결과와 분배 내역(SettlementItem)을 기록한다.

---

## 2. Settlement와 Payment 관계

- 하나의 Payment에는 하나의 Settlement만 생성 가능하다.
- DB unique 제약조건으로 1:1 관계를 강제한다.
- 이미 Settlement가 존재하는 Payment는 정산을 다시 실행할 수 없다.

```sql
UNIQUE (payment_id)
```

- 정산 조건은 다음과 같다
  - Payment.status == COMPLETED
  - Payment.amount > 0
  - Payment.settledAt = null

---

## 3. Settlement 생성 흐름

- 정산 실행 API 호출 시 다음 순서로 동작한다.
  - Payment 조회
  - Payment 정산 가능 여부 검증 (isPayable)
  - Settlement 생성 (READY 상태)
  - 상태 전이 : READY -> IN_PROGRESS
  - 정산 정책 계산
  - SettlementItem 생성
  - 상태 전이 : IN_PROGRESS -> COMPLETED
  - Payment.settledAt 설정
- 실패 시:
  - Settlement 상태를 FAILED로 변경
  - 트랜잭션은 REQUIRES_NEW로 처리하여 실패 상태는 보존

---

## 4. Settlement 타입

현재 구현 기준으로는 다음 타입이 존재한다.

### 4-1. NORMAL
- 기본 정산 타입
- Payment 1건당 1개의 NORMAL Settlement 생성

### 4-2. ADJUSTMENT
- 엔티티 구조상 지원 가능
- originalSettlement 참조 필드 존재
- 현재 비지니스 로직에서는 생성 로직 미구현

---

## 5. Settlement 상태

### 5-1. READY
- Settlement 생성 직후 상태

### 5-2. IN_PROGRESS
- 정산 정책 계산 및 분배 처리 중 상태

### 5-3. COMPLETED
- 정산 완료 상태
- completedAt 기록
- 이후 상태 변경 불가

### 5-4. FAILED
- 정산 처리 중 예외 발생 시 상태
- 재시도 가능 (IN_PROGRESS 또는 CANCELED로 전이 가능)

### 5-5. CANCELED
- 정산 무효 처리 상태
- 최종 상태

---

## 6. 상태 전이 규칙

READY -> IN_PROGRESS
IN_PROGRESS -> COMPLETED
IN_PROGRESS -> FAILED
FAILED -> IN_PROGRESS
FAILED -> CANCELED

COMPLETED 이후 변경 불가
CANCELED 이후 변경 불가

상태 전이는 SettlementStatus.canTransitTo 규칙을 따른다.

---

## 7. 불변성 원칙

Settlement는 과거 정산 기록을 보존하기 위한 도메인이다.

COMPLETED 이후 다음 값은 변경되지 않는다.

- totalAmount
- settlementPolicy
- type
- SettlementItem 구성

정산 변경이 필요한 경우,
기존 Settlement를 수정하지 않고
ADJUSTMENT 타입 Settlement를 추가 생성하는 방식으로 확장 가능하다. (현재 미구현)

---

## 8. 책임 범위

Settlement는 다음 책임을 가진다.

- 정산 실행 상태 관리
- 정산 금액 분배 계산 결과 기록
- SettlementItem 생성

Settlement는 다음 책임을 가지지 않는다.

- 결제 승인 처리
- Payment 상태 변경 (단, settlementAt 기록은 Business 계층에서 처리)