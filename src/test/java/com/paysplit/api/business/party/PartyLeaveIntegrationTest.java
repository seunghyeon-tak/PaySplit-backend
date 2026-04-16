package com.paysplit.api.business.party;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.PartyLeaveBusiness;
import com.paysplit.api.dto.party.response.PartyLeaveResponse;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.*;
import com.paysplit.db.enums.LeaveStatus;
import com.paysplit.db.repository.*;
import com.paysplit.support.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PartyLeaveIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private PartyLeaveBusiness partyLeaveBusiness;

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
    @DisplayName("파티 탈퇴 즉시탈퇴 가능")
    void leave_success_immediate_integration() {
        // given
        User leader = userRepository.save(UserFixture.activeUser());
        User user = userRepository.save(UserFixture.partyMemberUser());
        Party party = partyRepository.save(PartyFixture.recuitingParty(leader.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, user));
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.recuitingParty(policy, platform));
        Subscription subscription = subscriptionRepository.save(SubscriptionFixture.pendingPlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when
        PartyLeaveResponse response = partyLeaveBusiness.leave(party.getId(), user.getId());

        // then
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getPartyId()).isEqualTo(party.getId());
        assertThat(response.getPlanName()).isEqualTo(plan.getName());
        assertThat(response.getStatus()).isEqualTo(LeaveStatus.IMMEDIATE);
    }

    @Test
    @DisplayName("파티 탈퇴 예약 가능")
    void leave_success_reserved_integration() {
        // given
        User leader = userRepository.save(UserFixture.activeUser());
        User user = userRepository.save(UserFixture.partyMemberUser());
        Party party = partyRepository.save(PartyFixture.recuitingParty(leader.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, user));
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));
        Subscription subscription = subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when
        PartyLeaveResponse response = partyLeaveBusiness.leave(party.getId(), user.getId());

        // then
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getPartyId()).isEqualTo(party.getId());
        assertThat(response.getPlanName()).isEqualTo(plan.getName());
        assertThat(response.getStatus()).isEqualTo(LeaveStatus.RESERVED);
    }

    @Test
    @DisplayName("존재하지 않는 유저가 있으면 UserException 발생")
    void leave_exception_userNotFound() {
        // given
        Long userId = 999L;

        // when & then
        assertThatThrownBy(() -> partyLeaveBusiness.leave(1L, userId))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재 하지 않는 파티가 있으면 PartyException 발생")
    void leave_exception_partyNotFound() {
        // given
        User user = userRepository.save(UserFixture.partyMemberUser());

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyLeaveBusiness.leave(999L, user.getId()))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_FOUND.getMessage());
    }
}
