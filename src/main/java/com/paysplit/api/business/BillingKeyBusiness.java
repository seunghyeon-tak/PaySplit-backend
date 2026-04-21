package com.paysplit.api.business;

import com.paysplit.api.converter.BillingKeyConverter;
import com.paysplit.api.dto.billing.response.BillingKeyResponse;
import com.paysplit.api.service.BillingKeyService;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.client.TossPaymentClient;
import com.paysplit.common.client.dto.TossBillingKeyResponse;
import com.paysplit.db.domain.BillingKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional
public class BillingKeyBusiness {
    private final TossPaymentClient tossPaymentClient;
    private final BillingKeyService billingKeyService;
    private final BillingKeyConverter billingKeyConverter;

    public BillingKeyResponse issueBillingKey(Long userId, String customerKey, String authKey) {
        // 토스페이먼츠 빌링키 발급 API 호출
        TossBillingKeyResponse tossResponse = tossPaymentClient.issueBillingKey(customerKey, authKey);

        // 응답으로 받은 빌링키 정보 DB 저장
        BillingKey billingKey = billingKeyService.save(userId, tossResponse);

        // 응답 반환
        return billingKeyConverter.toResponse(billingKey);
    }
}
