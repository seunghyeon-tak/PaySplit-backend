package com.paysplit.api.business;

import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.repository.PaymentRepository;
import com.paysplit.db.repository.SettlementPolicyRepository;
import com.paysplit.db.repository.SettlementRepository;
import com.paysplit.support.PaymentFixture;
import com.paysplit.support.SettlementPolicyFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SettlementExecuteConcurrencyTest {
    @Autowired
    private SettlementExecuteBusiness settlementExecuteBusiness;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("동시에 정산 요청이 들어와도 settlement는 하나만 생성된다.")
    void concurrent_execute_test() throws Exception {
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Payment payment = paymentRepository.save(PaymentFixture.completedPayment(policy));

        SettlementExecuteRequest request = new SettlementExecuteRequest(payment.getId());

        int threadCount = 2;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    settlementExecuteBusiness.execute(request);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long count = settlementRepository.countByPayment(payment);

        assertThat(count).isEqualTo(1);
    }
}
