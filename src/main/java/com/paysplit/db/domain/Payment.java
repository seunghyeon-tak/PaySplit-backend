package com.paysplit.db.domain;

import com.paysplit.db.enums.PaymentMethod;
import com.paysplit.db.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SettlementPolicy settlementPolicy;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "payer_id", nullable = false)
    private Long payerId;  // 결제 요청자

    @Column(name = "external_payment_id", length = 100)
    private String externalPaymentId;  // 어디서 결제되었는가? (PG사 결제 ID)

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;  // 결제가 정산되었음을 나타내는 컬럼

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isPayable() {
        return status == PaymentStatus.COMPLETED
                && amount.compareTo(BigDecimal.ZERO) > 0
                && settledAt == null;
    }

    public void settle() {
        this.status = PaymentStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }
}
