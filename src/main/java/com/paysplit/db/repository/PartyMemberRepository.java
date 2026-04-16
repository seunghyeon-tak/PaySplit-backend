package com.paysplit.db.repository;

import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.PartyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    Optional<PartyMember> findByPartyAndUser(Party party, User user);

    int countByPartyIdAndStatus(Long partyId, PartyMemberStatus status);

    @Query("""
        select count(pm) > 0
        from PartyMember pm
        join Subscription s on s.party.id = pm.party.id
        where pm.user.id = :userId
            and s.plan.id = :planId
            and pm.status = :status
        """)
    boolean existsActiveByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Long planId, @Param("status") PartyMemberStatus status);


    @Query("""
        select pm
        from PartyMember pm
        join Subscription s on s.party.id = pm.party.id
        where pm.leaveRequestedAt is not null
        and pm.status = 'ACTIVE'
        and s.endedAt <= :now
        """)
    List<PartyMember> findExpiredLeaveRequests(@Param("now") LocalDateTime now);

    List<PartyMember> findByPartyIdAndStatus(Long partyId, PartyMemberStatus status);
}
