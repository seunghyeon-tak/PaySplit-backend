package com.paysplit.api.business.party;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.PartyCreateBusiness;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.service.WaitingQueueService;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.*;
import com.paysplit.db.enums.PartyMemberStatus;
import com.paysplit.db.enums.PartyStatus;
import com.paysplit.db.repository.*;
import com.paysplit.support.PlatformFixture;
import com.paysplit.support.SettlementPolicyFixture;
import com.paysplit.support.SubscriptionPlanFixture;
import com.paysplit.support.UserFixture;
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
public class PartyCreateIntegrationTest {
    @Autowired
    private PartyCreateBusiness partyCreateBusiness;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private WaitingQueueService waitingQueueService;

    @AfterEach
    void testDown() {
        waitingQueueService.clearWaitingQueue();
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
    @DisplayName("파티 생성 시 대기 큐에 있는 유저가 자동으로 파티 멤버로 추가된다.")
    void create_success_waitingQueueUserAutoMatched() {
        // given
        User leaderUser = userRepository.save(UserFixture.activeUser());
        User waitingUser = userRepository.save(UserFixture.partyMemberUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(leaderUser.getId(), null, Collections.emptyList())
        );

        waitingQueueService.addToWaitingQueue(plan.getId(), waitingUser.getId());

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(plan.getId())
                .build();

        // when
        partyCreateBusiness.create(leaderUser.getId(), request);

        // then
        Party party = partyRepository.findByLeaderId(leaderUser.getId()).orElseThrow();
        assertThat(partyMemberRepository.countByPartyIdAndStatus(party.getId(), PartyMemberStatus.ACTIVE)).isEqualTo(2);
    }

    @Test
    @DisplayName("파티 생성 시 DB에 파티가 저장된다.")
    void create_success_integration() {
        // given
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(plan.getId())
                .build();

        // when
        partyCreateBusiness.create(user.getId(), request);

        // then
        Party saveParty = partyRepository.findByLeaderId(user.getId()).orElseThrow();


        assertThat(saveParty).isNotNull();
        assertThat(saveParty.getStatus()).isEqualTo(PartyStatus.RECRUITING);
        assertThat(saveParty.getLeaderId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("파티 생성 시 파티장이 파티 멤버로 DB에 저장된다.")
    void create_success_leaderPartyMemberSaved() {
        // given
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(platformRepository.save(PlatformFixture.activePlatform()));
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(plan.getId())
                .build();

        // when
        partyCreateBusiness.create(user.getId(), request);

        // then
        Party savedParty = partyRepository.findByLeaderId(user.getId()).orElseThrow();
        PartyMember savedPartyMember = partyMemberRepository.findByPartyAndUser(savedParty, user).orElseThrow();

        assertThat(savedPartyMember).isNotNull();
        assertThat(savedPartyMember.getStatus()).isEqualTo(PartyMemberStatus.ACTIVE);
        assertThat(savedPartyMember.getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("파티 생성 시 초대 코드가 8자리 대문자로 생성된다.")
    void create_success_inviteCodeHasSize8UpperCase() {
        // given
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(platformRepository.save(PlatformFixture.activePlatform()));
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(plan.getId())
                .build();

        // when
        partyCreateBusiness.create(user.getId(), request);

        // then
        Party savedParty = partyRepository.findByLeaderId(user.getId()).orElseThrow();

        assertThat(savedParty).isNotNull();
        assertThat(savedParty.getInviteCode()).hasSize(8);
        assertThat(savedParty.getInviteCode()).isUpperCase();
    }

    @Test
    @DisplayName("탈퇴한 유저로 파티 생성 시 예외가 발생하고 DB에 저장되지 않는다.")
    void create_exception_PartyCreateWithdrawnUser() {
        // given
        User user = userRepository.save(UserFixture.withdrawnUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(platformRepository.save(PlatformFixture.activePlatform()));
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));

        // securityContext 인증 정보 확인
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList())
        );

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(plan.getId())
                .build();

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(user.getId(), request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.LEFT_USER.getMessage());

        // DB에 파티와 파티 멤버가 저장되지 않았는지 검증
        assertThat(partyRepository.findByLeaderId(user.getId())).isEmpty();
        assertThat(partyMemberRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재 하지 않는 유저로 파티 생성 시 예외가 발생한다.")
    void create_exception_partyCreateUserNotFound() {
        // given
        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(999L)
                .build();

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(999L, request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());

        assertThat(partyRepository.findAll()).isEmpty();
        assertThat(partyMemberRepository.findAll()).isEmpty();
    }
}
