package com.paysplit.db.domain;

import com.paysplit.db.enums.PartyMemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "party_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_party_members",
                        columnNames = {"party_id", "user_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PartyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PartyMemberStatus status;

    @Column(name = "leave_requested_at")
    private LocalDateTime leaveRequestedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getUserId() {
        return user.getId();
    }

    public void leave() {
        this.status = PartyMemberStatus.LEFT;
        this.leaveRequestedAt = LocalDateTime.now();
    }

    public void kick() {
        this.status = PartyMemberStatus.KICKED;
    }

    public void requestLeave() {
        this.leaveRequestedAt = LocalDateTime.now();
    }
}
