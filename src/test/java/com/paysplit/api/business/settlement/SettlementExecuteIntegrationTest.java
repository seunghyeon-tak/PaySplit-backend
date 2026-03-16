package com.paysplit.api.business.settlement;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.SettlementExecuteBusiness;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.enums.SettlementType;
import com.paysplit.db.repository.PaymentRepository;
import com.paysplit.db.repository.SettlementItemRepository;
import com.paysplit.db.repository.SettlementPolicyRepository;
import com.paysplit.db.repository.SettlementRepository;
import com.paysplit.support.PaymentFixture;
import com.paysplit.support.SettlementPolicyFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SettlementExecuteIntegrationTest {

    @Autowired
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementItemRepository settlementItemRepository;

    @AfterEach
    void tearDown() {
        // FK 순서 고려해서 역순으로 삭제
        settlementItemRepository.deleteAll();
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        settlementPolicyRepository.deleteAll();
    }

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
    @DisplayName("NORMAL 정산 후 settlement_items가 저장된다.")
    void execute_success_settlementItemsSaved() {
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
        Settlement settlement = settlementRepository
                .findByPaymentAndType(payment, SettlementType.NORMAL)
                .orElseThrow();

        List<?> items = settlementItemRepository.findAllBySettlementId(settlement.getId());
        assertThat(items).isNotEmpty();
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

        // when - 2차 실행
        settlementExecuteBusiness.execute(request);

        // then
        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();

        // settledAt이 변경되지 않았는지
        assertThat(updated.getSettledAt()).isNotNull();
        assertThat(updated.getSettledAt()).isEqualTo(firstSettledAt);

        // settlement가 1개만 생성됐는지
        long settlementCount = settlementRepository.countByPaymentAndType(payment, SettlementType.NORMAL);
        assertThat(settlementCount).isEqualTo(1);

        // settlement items도 중복 생성되지 않았는지
        Settlement settlement = settlementRepository
                .findByPaymentAndType(payment, SettlementType.NORMAL)
                .orElseThrow();

        List<?> items = settlementItemRepository.findAllBySettlementId(settlement.getId());
        assertThat(items).isNotEmpty();
    }
}