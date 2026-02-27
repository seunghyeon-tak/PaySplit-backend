package com.paysplit.db.repository;

import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.enums.SettlementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsByPaymentAndType(Payment payment, SettlementType type);

    long countByPayment(Payment payment);

    Optional<Settlement> findByPaymentAndType(Payment payment, SettlementType type);

    long countByPaymentAndType(Payment payment, SettlementType type);
}
