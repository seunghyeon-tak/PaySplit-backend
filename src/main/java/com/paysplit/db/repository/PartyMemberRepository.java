package com.paysplit.db.repository;

import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    Optional<PartyMember> findByPartyAndUser(Party party, User user);
}
