package com.paysplit.api.business.billing;

import com.paysplit.api.business.BillingKeyBusiness;
import com.paysplit.api.converter.BillingKeyConverter;
import com.paysplit.api.dto.billing.response.BillingKeyResponse;
import com.paysplit.api.service.BillingKeyService;
import com.paysplit.common.client.TossPaymentClient;
import com.paysplit.common.client.dto.TossBillingKeyResponse;
import com.paysplit.common.util.SecurityUtils;
import com.paysplit.db.domain.BillingKey;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillingKeyBusinessTest {
    @Mock
    private BillingKeyService billingKeyService;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private BillingKeyConverter billingKeyConverter;

    @InjectMocks
    private BillingKeyBusiness billingKeyBusiness;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("토스 페이먼츠 빌링키 발급 및 저장")
    void issueBillingKey_success() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        String customerKey = "aENcQAtPdYbTjGhtQnNVj";
        String authKey = "e_826EDB0730790E96F116FFF3799A65DE";

        TossBillingKeyResponse tossBillingKeyResponse = mock(TossBillingKeyResponse.class);
        BillingKey billingKey = mock(BillingKey.class);
        BillingKeyResponse response = mock(BillingKeyResponse.class);

        when(tossPaymentClient.issueBillingKey(customerKey, authKey)).thenReturn(tossBillingKeyResponse);
        when(billingKeyService.save(userId, tossBillingKeyResponse)).thenReturn(billingKey);
        when(billingKeyConverter.toResponse(billingKey)).thenReturn(response);

        // when
        BillingKeyResponse result = billingKeyBusiness.issueBillingKey(userId, customerKey, authKey);

        // then
        verify(tossPaymentClient).issueBillingKey(customerKey, authKey);
        verify(billingKeyService).save(userId, tossBillingKeyResponse);

        assertThat(result).isEqualTo(response);
    }
}
