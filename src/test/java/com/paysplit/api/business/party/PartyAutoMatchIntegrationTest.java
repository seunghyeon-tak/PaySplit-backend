package com.paysplit.api.business.party;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.PartyAutoMatchBusiness;
import com.paysplit.api.dto.party.request.PartyAutoMatchRequest;
import com.paysplit.api.dto.party.response.PartyAutoMatchResponse;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.*;
import com.paysplit.db.repository.*;
import com.paysplit.support.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
public class PartyAutoMatchIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PartyAutoMatchBusiness partyAutoMatchBusiness;

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
    @DisplayName("파티 자동 매칭 - WAITING")
    void waiting_match_success_integration() {
        // given
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(plan.getId())
                .build();

        // when
        PartyAutoMatchResponse response = partyAutoMatchBusiness.auto(user.getId(), request);

        // then
        assertThat(response.getStatus()).isEqualTo("WAITING");
        assertThat(response.getPartyId()).isEqualTo(null);
        assertThat(response.getPlanId()).isEqualTo(plan.getId());
        assertThat(response.getPlanName()).isEqualTo(plan.getName());
    }

    @Test
    @DisplayName("파티 자동 매칭 - JOINED")
    void joined_match_success_integration() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User joinUser = userRepository.save(UserFixture.partyMemberUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));
        Party party = partyRepository.save(PartyFixture.recuitingParty(leaderUser.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, leaderUser));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(joinUser.getId(), null, Collections.emptyList())
        );

        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(plan.getId())
                .build();

        // when
        PartyAutoMatchResponse response = partyAutoMatchBusiness.auto(joinUser.getId(), request);

        // then
        assertThat(response.getStatus()).isEqualTo("JOINED");
        assertThat(response.getPartyId()).isEqualTo(party.getId());
        assertThat(response.getPlanId()).isEqualTo(plan.getId());
        assertThat(response.getPlanName()).isEqualTo(plan.getName());
    }

    @Test
    @DisplayName("존재하지 않는 유저로 요청 시 UserException 발생")
    void get_exception_userNotFound() {
        // given
        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(1L)
                .build();

        // when & then
        assertThatThrownBy(() -> partyAutoMatchBusiness.auto(999L, request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("탈퇴 유저로 요청 시 UserException 발생")
    void get_exception_leftUser() {
        // given
        User user = userRepository.save(UserFixture.withdrawnUser());

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(1L)
                .build();

        // when & then
        assertThatThrownBy(() -> partyAutoMatchBusiness.auto(user.getId(), request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.LEFT_USER.getMessage());
    }

    @Test
    @DisplayName("이미 같은 플랜 파티에 참여 중일 때 PartyException 발생")
    void get_exception_alreadyJoined() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User joinUser = userRepository.save(UserFixture.partyMemberUser());
        Party party = partyRepository.save(PartyFixture.recuitingParty(leaderUser.getId()));
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, joinUser));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(joinUser.getId(), null, Collections.emptyList())
        );

        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(plan.getId())
                .build();

        // when & then
        assertThatThrownBy(() -> partyAutoMatchBusiness.auto(joinUser.getId(), request))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.ALREADY_JOINED.getMessage());
    }
}
