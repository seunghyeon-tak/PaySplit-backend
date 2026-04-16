package com.paysplit.api.business.party;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.PartyInviteJoinBusiness;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.*;
import com.paysplit.db.enums.PartyMemberStatus;
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
public class PartyInviteJoinIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;
    @Autowired
    private PartyInviteJoinBusiness partyInviteJoinBusiness;

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
    @DisplayName("파티 초대 코드로 정상 가입")
    void join_success_integration() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User user = userRepository.save(UserFixture.partyMemberUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));
        Party party = partyRepository.save(PartyFixture.recuitingParty(leaderUser.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, leaderUser));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when
        partyInviteJoinBusiness.join(party.getInviteCode(), user.getId());

        // then
        PartyMember savePartyMember = partyMemberRepository.findByPartyAndUser(party, user).orElseThrow();

        assertThat(savePartyMember.getUserId()).isEqualTo(user.getId());
        assertThat(savePartyMember.getStatus()).isEqualTo(PartyMemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 초대 코드로 가입 시 PartyException 발생")
    void join_exception_invalidInviteCode() {
        // given
        User user = userRepository.save(UserFixture.activeUser());

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyInviteJoinBusiness.join("INVALID_CODE", user.getId()))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("탈퇴한 유저가 가입 시 UserException 발생")
    void join_exception_withdrawnUser() {
        // given
        User user = userRepository.save(UserFixture.withdrawnUser());

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyInviteJoinBusiness.join("INVALID_CODE", user.getId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.LEFT_USER.getMessage());
    }

    @Test
    @DisplayName("파티가 꽉 찼을때 PartyException 발생")
    void join_exception_memberMax() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User user = userRepository.save(UserFixture.partyMemberUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.recuitingParty(policy, platform));
        Party party = partyRepository.save(PartyFixture.recuitingParty(leaderUser.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, leaderUser));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyInviteJoinBusiness.join(party.getInviteCode(), user.getId()))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_MEMBER_FULL.getMessage());
    }

    @Test
    @DisplayName("이미 같은 플랜 파티에 참여 중일때 PartyException 발생")
    void join_exception_duplicationParty() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.recuitingParty(policy, platform));
        Party party = partyRepository.save(PartyFixture.recuitingParty(leaderUser.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, leaderUser));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, user));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        // when & then
        assertThatThrownBy(() -> partyInviteJoinBusiness.join(party.getInviteCode(), user.getId()))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.ALREADY_JOINED.getMessage());
    }
}
