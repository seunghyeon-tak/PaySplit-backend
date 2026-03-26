package com.paysplit.db.repository;

import com.paysplit.db.domain.Party;
import com.paysplit.db.enums.PartyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartyRepository extends JpaRepository<Party, Long> {
    boolean existsByInviteCode(String code);

    Optional<Party> findByLeaderId(Long leaderId);

    Optional<Party> findByInviteCodeAndStatus(String inviteCode, PartyStatus status);

    @Query("""
    select p from Party p
    join Subscription s on s.party.id = p.id
    join SubscriptionPlan sp on sp.id = s.plan.id
    where sp.id = :planId
    and p.status = 'RECRUITING'
    and (select count(pm) from PartyMember pm where pm.party.id = p.id and pm.status = 'ACTIVE') < sp.maxMembers
    """)
    Optional<Party> findAvailableParty(@Param("planId") Long planId);
}
