package com.paysplit.api.service;

import com.paysplit.api.dto.settlement.result.SettlementItemResult;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.enums.FeeType;
import com.paysplit.db.enums.ReceiverType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.paysplit.common.error.settlement.SettlementErrorCode.*;

@Service
@RequiredArgsConstructor
public class SettlementPolicyService {
    private static final int MONEY_SCALE = 0;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.DOWN;

    public List<SettlementItemResult> calculate(Settlement settlement) {

        BigDecimal totalAmount = settlement.getTotalAmount();
        SettlementPolicy policy = settlement.getSettlementPolicy();

        int partySize = 4; // TODO: 임의 값 추후 수정 필요
        if (partySize <= 0) {
            throw new SettlementException(INVALID_PARTY_SIZE);
        }

        // 1. 기본 1인 결제 금액
        BigDecimal basePayAmount = totalAmount.divide(
                BigDecimal.valueOf(partySize),
                MONEY_SCALE,
                RoundingMode.DOWN
        );

        // 1-1) 나머지(잔돈) = total - base * partySize (항상 0 이상)
        BigDecimal remainder = totalAmount.subtract(
                basePayAmount.multiply(BigDecimal.valueOf(partySize))
        );

        // 2. 파티장 할인 금액 계산 (정책 기반)
        BigDecimal leaderDiscountAmount = calculatePolicyAmount(
                policy.getLeaderShareType(),
                policy.getLeaderShareValue(),
                totalAmount
        );

        // 할인 금액은 0이상이어야함
        if (leaderDiscountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new SettlementException(INVALID_DISTRIBUTION_AMOUNT);
        }

        // 3. 파티장 결제 금액 = base - discount + remainder
        // remainder는 파티장에게 몰아줘서 전체 합계가 total과 정확히 같아지도록 함
        BigDecimal leaderPayAmount = basePayAmount
                .subtract(leaderDiscountAmount)
                .add(remainder);
        if (leaderPayAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new SettlementException(INVALID_DISTRIBUTION_AMOUNT);
        }

        // 4. 플랫폼 지원 금액 (파티장 할인과 동일)
        BigDecimal platformSupportAmount = leaderDiscountAmount;

        // 5. 정합성 검증
        BigDecimal calculatedTotal =
                basePayAmount.multiply(BigDecimal.valueOf(partySize - 1))
                        .add(leaderPayAmount)
                        .add(platformSupportAmount);

        if (calculatedTotal.compareTo(totalAmount) != 0) {
            throw new SettlementException(INTERNAL_SERVER_ERROR);
        }

        // 6. 결과 생성
        List<SettlementItemResult> results = new ArrayList<>();

        // 파티원 결제 (partySize - 1 명)
        for (int i = 0; i < partySize - 1; i++) {
            results.add(
                    SettlementItemResult.builder()
                            .receiverType(ReceiverType.MEMBER_PAYMENT)
                            .receiverId(1000L + i)  // todo : 임시 유저 ID
                            .amount(basePayAmount)
                            .build()
            );
        }

        // 파티장 결제 (잔돈 포함)
        results.add(
                SettlementItemResult.builder()
                        .receiverType(ReceiverType.LEADER_PAYMENT)
                        .receiverId(999L)  // todo : 임시 파티장 ID
                        .amount(leaderPayAmount)
                        .build()
        );

        // 플랫폼 지원
        results.add(
                SettlementItemResult.builder()
                        .receiverType(ReceiverType.PLATFORM_SUPPORT)
                        .receiverId(0L)  // todo : 플랫폼은 고정 ID 일단 임시
                        .amount(platformSupportAmount)
                        .build()
        );

        return results;
    }

    /*
     * 정책 금액 계산
     * - FIXED : 그대로
     * - RATE : total * value 를 원 단위로 반올림(여기서는 DOWN)
     *
     * 주의 : RATE의 value가 0.1(=10%)인지 10(=10%)인지 스펙을 명확히 해야 함.
     * 현재는 "0~1 비율"이라고 가정.
     * */
    private BigDecimal calculatePolicyAmount(
            FeeType type,
            BigDecimal value,
            BigDecimal totalAmount
    ) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (type == FeeType.FIXED) {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new SettlementException(INVALID_DISTRIBUTION_AMOUNT);
            }
            return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
        }

        // RATE는 0~1 비율로 강제 (ex. 10% -> 0.10)
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new SettlementException(INVALID_DISTRIBUTION_AMOUNT);
        }
        return totalAmount
                .multiply(value)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
