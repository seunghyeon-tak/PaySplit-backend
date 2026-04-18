package com.paysplit.api.business.party;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.PartyDisbandBusiness;
import com.paysplit.api.dto.party.response.PartyDisbandResponse;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.db.domain.*;
import com.paysplit.db.enums.PartyStatus;
import com.paysplit.db.repository.*;
import com.paysplit.support.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PartyDisbandIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private PartyDisbandBusiness partyDisbandBusiness;

    @AfterEach
    void testDown() {
        subscriptionRepository.deleteAll();
        partyMemberRepository.deleteAll();
        partyRepository.deleteAll();
        subscriptionPlanRepository.deleteAll();
        platformRepository.deleteAll();
        settlementPolicyRepository.deleteAll();
        userRepository.deleteAll();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("파티 즉시 해산")
    void disband_success_immediate_integration() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        Party party = partyRepository.save(PartyFixture.activeParty(leaderUser.getId()));
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.recuitingParty(policy, platform));
        Subscription subscription = subscriptionRepository.save(SubscriptionFixture.pendingPlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(leaderUser.getId(), null, Collections.emptyList())
        );

        // when
        PartyDisbandResponse response = partyDisbandBusiness.disband(party.getId(), leaderUser.getId());

        // then
        Party disbandedParty = partyRepository.findById(party.getId()).orElseThrow();
        assertThat(disbandedParty.getStatus()).isEqualTo(PartyStatus.DISBANDED);
        assertThat(disbandedParty.getInviteCode()).isNull();
        assertThat(response.getPartyId()).isEqualTo(party.getId());
        assertThat(response.getPlanName()).isEqualTo(subscription.getPlan().getName());
        assertThat(response.getDisbandedAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PartyStatus.DISBANDED);
    }

    @Test
    @DisplayName("존재하지 않는 파티 해산 시 PartyException 발생")
    void disband_exception_partyNotFound() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(leaderUser.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyDisbandBusiness.disband(999L, leaderUser.getId()))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("파티장이 아닐 때 PartyException 발생")
    void disband_exception_notLeader() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User otherUser = userRepository.save(UserFixture.partyMemberUser());
        Party party = partyRepository.save(PartyFixture.activeParty(leaderUser.getId()));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(leaderUser.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyDisbandBusiness.disband(party.getId(), otherUser.getId()))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_LEADER.getMessage());
    }
}
