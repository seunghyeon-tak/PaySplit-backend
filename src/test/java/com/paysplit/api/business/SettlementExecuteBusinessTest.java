package com.paysplit.api.business;

import com.paysplit.api.converter.SettlementExecuteConverter;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.api.dto.settlement.response.SettlementExecuteResponse;
import com.paysplit.api.service.PaymentService;
import com.paysplit.api.service.SettlementPolicyService;
import com.paysplit.api.service.SettlementService;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementExecuteBusinessTest {
    @Mock
    private SettlementService settlementService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SettlementPolicyService settlementPolicyService;

    @Mock
    private SettlementExecuteConverter settlementExecuteConverter;

    @InjectMocks
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @DisplayName("정산이 정상적으로 실행된다.")
    @Test
    void execute_success() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = new SettlementExecuteRequest(paymentId);
        Payment payment = mock(Payment.class);
        Settlement settlement = mock(Settlement.class);
        SettlementExecuteResponse response = mock(SettlementExecuteResponse.class);

        when(paymentService.getById(paymentId)).thenReturn(payment);
        when(payment.isPayable()).thenReturn(true);
        when(settlementService.createSettlement(payment)).thenReturn(settlement);
        when(settlementPolicyService.calculate(settlement)).thenReturn(List.of());
        when(settlementExecuteConverter.toResponse(settlement)).thenReturn(response);

        // when
        SettlementExecuteResponse result = settlementExecuteBusiness.execute(request);

        // then
        verify(settlementService).createSettlement(payment);
        verify(settlementPolicyService).calculate(settlement);
        verify(settlementService).createSettlementItems(eq(settlement), any());

        assertThat(result).isEqualTo(response);
    }

    @DisplayName("결제 상태가 유효하지 않으면 예외가 발생한다.")
    @Test
    void execute_whenInvalidPaymentState_throwException() {
        // given
        Long paymentId = 1L;
        SettlementExecuteRequest request = new SettlementExecuteRequest(paymentId);

        Payment payment = mock(Payment.class);

        when(paymentService.getById(paymentId)).thenReturn(payment);
        when(payment.isPayable()).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
                settlementExecuteBusiness.execute(request)
        ).isInstanceOf(PaymentException.class);
    }
}
