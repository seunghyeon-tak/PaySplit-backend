package com.paysplit.db.repository;

import com.paysplit.db.domain.Party;
import com.paysplit.db.enums.PartyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyRepository extends JpaRepository<Party, Long> {
    boolean existsByInviteCode(String code);

    Optional<Party> findByLeaderId(Long leaderId);

    Optional<Party> findByInviteCodeAndStatus(String inviteCode, PartyStatus status);
}
