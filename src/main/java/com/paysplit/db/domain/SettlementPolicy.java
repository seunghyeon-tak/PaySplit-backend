package com.paysplit.db.domain;

import com.paysplit.db.enums.FeeType;
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
@Table(name = "settlement_policies")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class SettlementPolicy {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "policy_code", nullable = false)
    private String policyCode;

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_fee_type", nullable = false)
    private FeeType platformFeeType;

    @Column(name = "platform_fee_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal platformFeeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "leader_share_type", nullable = false)
    private FeeType leaderShareType;

    @Column(name = "leader_share_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal leaderShareValue;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
