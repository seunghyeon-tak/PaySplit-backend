package com.paysplit.db.domain;

import com.paysplit.common.error.settlement.SettlementErrorCode;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.db.enums.SettlementStatus;
import com.paysplit.db.enums.SettlementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.paysplit.common.error.settlement.SettlementErrorCode.*;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_payment",
                        columnNames = {"payment_id", "type"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Settlement {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 재정산 또는 조정 시 참조하는 원본 Settlement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_settlement_id")
    private Settlement originalSettlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SettlementPolicy settlementPolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementType type;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void changeStatus(SettlementStatus next) {
        if (!this.status.canTransitTo(next)) {
            throw new SettlementException(INVALID_SETTLEMENT_STATE);
        }
        this.status = next;

        if (next == SettlementStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
