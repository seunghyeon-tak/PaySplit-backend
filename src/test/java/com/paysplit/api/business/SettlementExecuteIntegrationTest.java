package com.paysplit.api.business;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.enums.PaymentStatus;
import com.paysplit.db.repository.PaymentRepository;
import com.paysplit.db.repository.SettlementPolicyRepository;
import com.paysplit.support.PaymentFixture;
import com.paysplit.support.SettlementPolicyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SettlementExecuteIntegrationTest {

    @Autowired
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Test
    @DisplayName("정산이 실제 DB에 반영된다.")
    void execute_success_integration() {
        // given
        SettlementPolicy settlementPolicy = settlementPolicyRepository
                .save(SettlementPolicyFixture.activePolicy());

        Payment payment = paymentRepository
                .save(PaymentFixture.completedPayment(settlementPolicy));

        SettlementExecuteRequest request = new SettlementExecuteRequest(payment.getId());

        // when
        settlementExecuteBusiness.execute(request);

        // then
        Payment updated = paymentRepository
                .findById(payment.getId())
                .orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.SETTLED);
        assertThat(updated.getSettledAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 정산된 결제는 다시 정산할 수 없다.")
    void execute_fail_already_settled() {
        // given
        SettlementPolicy policy = settlementPolicyRepository
                .save(SettlementPolicyFixture.activePolicy());

        Payment payment = paymentRepository
                .save(PaymentFixture.completedPayment(policy));

        SettlementExecuteRequest request = new SettlementExecuteRequest(payment.getId());

        settlementExecuteBusiness.execute(request);

        // when & then
        assertThatThrownBy(() -> settlementExecuteBusiness.execute(request))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("잘못된 결제 상태");
    }
}
