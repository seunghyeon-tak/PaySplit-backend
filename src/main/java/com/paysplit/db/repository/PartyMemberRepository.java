package com.paysplit.db.repository;

import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.PartyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    Optional<PartyMember> findByPartyAndUser(Party party, User user);

    int countByPartyIdAndStatus(Long partyId, PartyMemberStatus status);
}
