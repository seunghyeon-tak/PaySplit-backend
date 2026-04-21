package com.paysplit.db.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "billing_keys")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BillingKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "billing_key", length = 200, nullable = false)
    private String billingKey;

    @Column(name = "customer_key", length = 300, nullable = false)
    private String customerKey;

    @Column(length = 14)
    private String mid;

    @Column(length = 20)
    private String method;

    @Column(name = "authenticated_at")
    private LocalDateTime authenticatedAt;

    @Column(name = "card_issuer_code", length = 2)
    private String cardIssuerCode;

    @Column(name = "card_acquirer_code", length = 2)
    private String cardAcquirerCode;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "card_type", length = 10)
    private String cardType;

    @Column(name = "card_owner_type", length = 10)
    private String cardOwnerType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
