package com.paysplit.api.business.settlement;

import com.paysplit.api.business.SettlementExecuteBusiness;
import com.paysplit.api.converter.SettlementExecuteConverter;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.api.dto.settlement.response.SettlementExecuteResponse;
import com.paysplit.api.dto.settlement.result.SettlementItemResult;
import com.paysplit.api.service.PaymentService;
import com.paysplit.api.service.SettlementFailureService;
import com.paysplit.api.service.SettlementPolicyService;
import com.paysplit.api.service.SettlementService;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.enums.SettlementStatus;
import com.paysplit.db.enums.SettlementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementExecuteBusinessTest {

    @Mock
    private SettlementService settlementService;
    @Mock
    private SettlementFailureService settlementFailureService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private SettlementPolicyService settlementPolicyService;
    @Mock
    private SettlementExecuteConverter settlementExecuteConverter;

    @InjectMocks
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @DisplayName("NORMAL 정산이 정상적으로 실행된다.")
    @Test
    void execute_normal_success() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.NORMAL)
                .build();

        Payment payment = mock(Payment.class);
        Settlement settlement = mock(Settlement.class);
        SettlementExecuteResponse response = mock(SettlementExecuteResponse.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(payment.isPayable()).thenReturn(true);
        when(settlementService.findByPaymentAndType(payment, SettlementType.NORMAL))
                .thenReturn(Optional.empty());
        when(settlementService.createOrGetSettlement(eq(payment), eq(SettlementType.NORMAL), isNull()))
                .thenReturn(settlement);
        when(settlement.getStatus()).thenReturn(SettlementStatus.READY);

        List<SettlementItemResult> results = List.of(SettlementItemResult.builder().build());
        when(settlementPolicyService.calculate(settlement)).thenReturn(results);
        when(settlementExecuteConverter.toResponse(settlement)).thenReturn(response);

        // when
        SettlementExecuteResponse result = settlementExecuteBusiness.execute(request);

        // then
        verify(settlementService).createOrGetSettlement(eq(payment), eq(SettlementType.NORMAL), isNull());
        verify(settlementPolicyService).calculate(settlement);
        verify(settlementService).createSettlementItems(settlement, results);
        verify(settlement).changeStatus(SettlementStatus.IN_PROGRESS);
        verify(settlement).changeStatus(SettlementStatus.COMPLETED);
        verify(paymentService).settleIfNotSettled(payment.getId());
        assertThat(result).isEqualTo(response);
    }

    @DisplayName("결제 상태가 유효하지 않으면 예외가 발생한다.")
    @Test
    void execute_whenInvalidPaymentState_throwException() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.NORMAL)
                .build();

        Payment payment = mock(Payment.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(settlementService.findByPaymentAndType(payment, SettlementType.NORMAL))
                .thenReturn(Optional.empty());
        when(payment.isPayable()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> settlementExecuteBusiness.execute(request))
                .isInstanceOf(PaymentException.class);

        verify(settlementService, never()).createOrGetSettlement(any(), any(), any());
        verify(settlementPolicyService, never()).calculate(any());
    }

    @DisplayName("이미 COMPLETED인 정산이면 멱등하게 반환하고 계산/저장을 수행하지 않는다.")
    @Test
    void execute_whenSettlementCompleted_returnIdempotent() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.NORMAL)
                .build();

        Payment payment = mock(Payment.class);
        Settlement settlement = mock(Settlement.class);
        SettlementExecuteResponse response = mock(SettlementExecuteResponse.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(settlementService.findByPaymentAndType(payment, SettlementType.NORMAL))
                .thenReturn(Optional.of(settlement));
        when(settlement.getStatus()).thenReturn(SettlementStatus.COMPLETED);
        when(settlementExecuteConverter.toResponse(settlement)).thenReturn(response);

        // when
        SettlementExecuteResponse result = settlementExecuteBusiness.execute(request);

        // then
        verify(settlementPolicyService, never()).calculate(any());
        verify(settlementService, never()).createSettlementItems(any(), any());
        verify(paymentService, never()).settleIfNotSettled(anyLong());
        assertThat(result).isEqualTo(response);
    }

    @DisplayName("이미 IN_PROGRESS인 정산이면 멱등하게 반환하고 계산/저장을 수행하지 않는다.")
    @Test
    void execute_whenSettlementInProgress_returnIdempotent() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.NORMAL)
                .build();

        Payment payment = mock(Payment.class);
        Settlement settlement = mock(Settlement.class);
        SettlementExecuteResponse response = mock(SettlementExecuteResponse.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(settlementService.findByPaymentAndType(payment, SettlementType.NORMAL))
                .thenReturn(Optional.of(settlement));
        when(settlement.getStatus()).thenReturn(SettlementStatus.IN_PROGRESS);
        when(settlementExecuteConverter.toResponse(settlement)).thenReturn(response);

        // when
        SettlementExecuteResponse result = settlementExecuteBusiness.execute(request);

        // then
        verify(settlementPolicyService, never()).calculate(any());
        verify(settlementService, never()).createSettlementItems(any(), any());
        verify(paymentService, never()).settleIfNotSettled(anyLong());
        assertThat(result).isEqualTo(response);
    }

    @DisplayName("FAILED 상태 정산은 prepareRetry가 호출되고 재시도된다.")
    @Test
    void execute_whenSettlementFailed_retryAndSucceed() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.NORMAL)
                .build();

        Payment payment = mock(Payment.class);
        Settlement settlement = mock(Settlement.class);
        SettlementExecuteResponse response = mock(SettlementExecuteResponse.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(payment.isPayable()).thenReturn(true);
        when(settlementService.findByPaymentAndType(payment, SettlementType.NORMAL))
                .thenReturn(Optional.empty());
        when(settlementService.createOrGetSettlement(eq(payment), eq(SettlementType.NORMAL), isNull()))
                .thenReturn(settlement);
        when(settlement.getStatus()).thenReturn(SettlementStatus.FAILED);

        List<SettlementItemResult> results = List.of(SettlementItemResult.builder().build());
        when(settlementPolicyService.calculate(settlement)).thenReturn(results);
        when(settlementExecuteConverter.toResponse(settlement)).thenReturn(response);

        // when
        settlementExecuteBusiness.execute(request);

        // then
        verify(settlementService).prepareRetry(settlement);
        verify(settlementPolicyService).calculate(settlement);
        verify(settlementService).createSettlementItems(settlement, results);
    }

    @DisplayName("정산 도중 예외 발생 시 markFailed가 호출된다.")
    @Test
    void execute_whenExceptionDuringSettlement_markFailed() {
        // given
        Long paymentId = 1L;
        Long settlementId = 100L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.NORMAL)
                .build();

        Payment payment = mock(Payment.class);
        Settlement settlement = mock(Settlement.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(payment.isPayable()).thenReturn(true);
        when(settlementService.findByPaymentAndType(payment, SettlementType.NORMAL))
                .thenReturn(Optional.empty());
        when(settlementService.createOrGetSettlement(eq(payment), eq(SettlementType.NORMAL), isNull()))
                .thenReturn(settlement);
        when(settlement.getStatus()).thenReturn(SettlementStatus.READY);
        when(settlement.getId()).thenReturn(settlementId);
        when(settlementPolicyService.calculate(settlement)).thenThrow(new RuntimeException("계산 오류"));

        // when & then
        assertThatThrownBy(() -> settlementExecuteBusiness.execute(request))
                .isInstanceOf(RuntimeException.class);

        verify(settlementFailureService).markFailed(settlementId);
    }

    @DisplayName("REVERSAL 요청이면 원본 정산을 조회하고 payment+type 기준으로 멱등 생성한다.")
    @Test
    void execute_reversal_success() {
        // given
        Long paymentId = 1L;
        Long originalSettlementId = 10L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.REVERSAL)
                .originalSettlementId(originalSettlementId)
                .build();

        Payment payment = mock(Payment.class);
        Payment originalPayment = mock(Payment.class);
        Settlement original = mock(Settlement.class);
        Settlement settlement = mock(Settlement.class);
        SettlementExecuteResponse response = mock(SettlementExecuteResponse.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(payment.getId()).thenReturn(paymentId);

        when(settlementService.findByPaymentAndType(payment, SettlementType.REVERSAL))
                .thenReturn(Optional.empty());
        when(settlementService.getById(originalSettlementId)).thenReturn(original);
        when(original.getPayment()).thenReturn(originalPayment);
        when(originalPayment.getId()).thenReturn(paymentId);

        when(settlementService.createOrGetSettlement(payment, SettlementType.REVERSAL, original))
                .thenReturn(settlement);
        when(settlement.getStatus()).thenReturn(SettlementStatus.READY);

        List<SettlementItemResult> results = List.of(SettlementItemResult.builder().build());
        when(settlementPolicyService.calculate(settlement)).thenReturn(results);
        when(settlementExecuteConverter.toResponse(settlement)).thenReturn(response);

        // when
        SettlementExecuteResponse result = settlementExecuteBusiness.execute(request);

        // then
        verify(settlementService).getById(originalSettlementId);
        verify(settlementService).createOrGetSettlement(payment, SettlementType.REVERSAL, original);
        verify(settlementPolicyService).calculate(settlement);
        verify(settlementService).createSettlementItems(settlement, results);
        verify(paymentService, never()).settleIfNotSettled(anyLong()); // REVERSAL은 settledAt 마킹 안 함
        assertThat(result).isEqualTo(response);
    }

    @DisplayName("REVERSAL 요청 시 originalSettlementId가 없으면 예외가 발생한다.")
    @Test
    void execute_reversal_withoutOriginalSettlementId_throwException() {
        // given
        Long paymentId = 1L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.REVERSAL)
                .originalSettlementId(null)
                .build();

        Payment payment = mock(Payment.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(settlementService.findByPaymentAndType(payment, SettlementType.REVERSAL))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settlementExecuteBusiness.execute(request))
                .isInstanceOf(SettlementException.class);

        verify(settlementService, never()).getById(any());
    }

    @DisplayName("REVERSAL 요청 시 원본 정산의 payment가 다르면 예외가 발생한다.")
    @Test
    void execute_reversal_withMismatchedPayment_throwException() {
        // given
        Long paymentId = 1L;
        Long originalSettlementId = 10L;
        Long differentPaymentId = 99L;

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(paymentId)
                .type(SettlementType.REVERSAL)
                .originalSettlementId(originalSettlementId)
                .build();

        Payment payment = mock(Payment.class);
        Payment differentPayment = mock(Payment.class);
        Settlement original = mock(Settlement.class);

        when(paymentService.getByIdForUpdate(paymentId)).thenReturn(payment);
        when(payment.getId()).thenReturn(paymentId);
        when(settlementService.findByPaymentAndType(payment, SettlementType.REVERSAL))
                .thenReturn(Optional.empty());
        when(settlementService.getById(originalSettlementId)).thenReturn(original);
        when(original.getPayment()).thenReturn(differentPayment);
        when(differentPayment.getId()).thenReturn(differentPaymentId);

        // when & then
        assertThatThrownBy(() -> settlementExecuteBusiness.execute(request))
                .isInstanceOf(SettlementException.class);

        verify(settlementService, never()).createOrGetSettlement(any(), any(), any());
    }
}