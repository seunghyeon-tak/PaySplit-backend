package com.paysplit.api.business;

import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.db.domain.Payment;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SettlementExecuteConcurrencyTest {

    @Autowired
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("동시에 NORMAL 정산 요청이 들어와도 payment+type 기준 settlement는 하나만 생성된다.")
    void concurrent_execute_test() throws Exception {
        // given
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Payment payment = paymentRepository.save(PaymentFixture.completedPayment(policy));

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(payment.getId())
                .type(SettlementType.NORMAL)
                .build();

        int threadCount = 2;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    settlementExecuteBusiness.execute(request);
                } catch (Exception ignored) {
                    // 멱등/락/유니크 충돌 상황에서 일부 스레드가 실패할 수도 있으니 무시
                    // (원하면 여기서 예외 카운트 세서 "실패해도 최종 결과가 1개"만 검증해도 됨)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        long count = settlementRepository.countByPaymentAndType(payment, SettlementType.NORMAL);
        assertThat(count).isEqualTo(1);
    }
}