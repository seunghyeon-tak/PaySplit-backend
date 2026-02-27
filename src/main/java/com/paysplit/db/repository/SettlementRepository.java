package com.paysplit.db.repository;

import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsByPayment(Payment payment);
    long countByPayment(Payment payment);

    Optional<Settlement> findByPayment(Payment payment);
}
