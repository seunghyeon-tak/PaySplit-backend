package com.paysplit.api.business;

import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.db.domain.Payment;
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

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SettlementExecuteConcurrencyTest {

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
        settlementItemRepository.deleteAll();
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        settlementPolicyRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 NORMAL 정산 요청이 들어와도 settlement는 하나만 생성된다.")
    void concurrent_execute_onlyOneSettlementCreated() throws Exception {
        // given
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Payment payment = paymentRepository.save(PaymentFixture.completedPayment(policy));

        SettlementExecuteRequest request = SettlementExecuteRequest.builder()
                .paymentId(payment.getId())
                .type(SettlementType.NORMAL)
                .build();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount); // 모든 스레드가 준비될 때까지 대기
        CountDownLatch startLatch = new CountDownLatch(1);           // 동시에 출발시키기 위한 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount);  // 모든 스레드 완료 대기

        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();       // 준비 완료 신호
                    startLatch.await();           // 출발 신호 대기 (최대한 동시에 실행되도록)
                    settlementExecuteBusiness.execute(request);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();  // 모든 스레드 준비될 때까지 대기
        startLatch.countDown(); // 동시에 출발
        doneLatch.await();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        long count = settlementRepository.countByPaymentAndType(payment, SettlementType.NORMAL);
        assertThat(count).isEqualTo(1);

        // 발생한 예외 로깅 (실패 원인 추적용)
        if (!exceptions.isEmpty()) {
            System.out.println("동시성 테스트 중 발생한 예외 수: " + exceptions.size());
            exceptions.forEach(e -> System.out.println(" - " + e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }
}