package com.paysplit.db.domain;

import com.paysplit.db.enums.PartyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "parties")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PartyStatus status;

    @Column(name = "invite_code", length = 50, unique = true)
    private String inviteCode;

    @Column(name = "disband_requested_at")
    private LocalDateTime disbandRequestedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void disband() {
        this.status = PartyStatus.DISBANDED;
        this.inviteCode = null;
    }

    public void requestDisband() {
        this.disbandRequestedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = PartyStatus.ACTIVE;
    }
}
