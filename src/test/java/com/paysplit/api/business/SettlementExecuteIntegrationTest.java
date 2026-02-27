package com.paysplit.api.business;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.enums.SettlementType;
import com.paysplit.db.repository.PaymentRepository;
import com.paysplit.db.repository.SettlementPolicyRepository;
import com.paysplit.db.repository.SettlementRepository;
import com.paysplit.support.PaymentFixture;
import com.paysplit.support.SettlementPolicyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SettlementExecuteIntegrationTest {

    @Autowired
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("NORMAL 정산이 실제 DB에 반영된다.")
    void execute_success_integration() {
        // given
        SettlementPolicy settlementPolicy = settlementPolicyRepository
                .save(SettlementPolicyFixture.activePolicy());

        Payment payment = paymentRepository
                .save(PaymentFixture.completedPayment(settlementPolicy));

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(payment.getId())
                .type(SettlementType.NORMAL)
                .build();

        // when
        settlementExecuteBusiness.execute(request);

        // then
        Payment updated = paymentRepository
                .findById(payment.getId())
                .orElseThrow();

        assertThat(updated.getSettledAt()).isNotNull();

        assertThat(settlementRepository.findByPaymentAndType(updated, SettlementType.NORMAL))
                .isPresent();
    }

    @Test
    @DisplayName("이미 정산된 결제에 대해 동일 요청을 재호출해도 멱등하게 처리된다.")
    void execute_idempotent_whenAlreadySettled() {
        // given
        SettlementPolicy policy = settlementPolicyRepository
                .save(SettlementPolicyFixture.activePolicy());

        Payment payment = paymentRepository
                .save(PaymentFixture.completedPayment(policy));

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(payment.getId())
                .type(SettlementType.NORMAL)
                .build();

        // 1차 실행
        settlementExecuteBusiness.execute(request);

        LocalDateTime firstSettledAt = paymentRepository.findById(payment.getId())
                .orElseThrow()
                .getSettledAt();

        // when
        settlementExecuteBusiness.execute(request);

        // then
        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updated.getSettledAt()).isNotNull();
        assertThat(updated.getSettledAt()).isEqualTo(firstSettledAt);

        Settlement settlement = settlementRepository.findByPaymentAndType(updated, SettlementType.NORMAL)
                .orElseThrow();

        assertThat(settlement.getType()).isEqualTo(SettlementType.NORMAL);
    }
}