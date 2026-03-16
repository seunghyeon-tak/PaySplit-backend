package com.paysplit.api.business.party;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.PartyFindByCodeBusiness;
import com.paysplit.api.dto.party.response.PartyFindByCodeResponse;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.SubscriptionService;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.db.domain.*;
import com.paysplit.db.repository.*;
import com.paysplit.support.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PartyFindByCodeIntegrationTest {

    @Autowired
    private PartyFindByCodeBusiness partyFindByCodeBusiness;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SettlementPolicyRepository settlementPolicyRepository;

    @AfterEach
    void testDown() {
        subscriptionRepository.deleteAll();
        partyMemberRepository.deleteAll();
        partyRepository.deleteAll();
        subscriptionPlanRepository.deleteAll();
        platformRepository.deleteAll();
        settlementPolicyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("파티 조회")
    void get_success_integration() {
        // given
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));
        Party party = partyRepository.save(PartyFixture.recuitingParty(user.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, user));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        String inviteCode = party.getInviteCode();

        // when
        PartyFindByCodeResponse response = partyFindByCodeBusiness.get(inviteCode);

        // then
        assertThat(response.getPlatformName()).isEqualTo(platform.getName());
        assertThat(response.getLeaderName()).isEqualTo(user.getName());
        assertThat(response.getCurrentMembers()).isEqualTo(1);
        assertThat(response.getMaxMembers()).isEqualTo(plan.getMaxMembers());
    }

    @Test
    @DisplayName("존재하지 않는 초대 코드로 조회시 PartyException 발생")
    void get_exception_invalidInviteCode() {
        // given
        String inviteCode = "INVALID_CODE";

        // when & then
        assertThatThrownBy(() -> partyFindByCodeBusiness.get(inviteCode))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("RECRUITING이 아닌 파티 초대 코드로 조회 시 PartyException 발생")
    void get_exception_recruiting() {
        // given
        User user = userRepository.save(UserFixture.activeUser());
        SettlementPolicy policy = settlementPolicyRepository.save(SettlementPolicyFixture.activePolicy());
        Platform platform = platformRepository.save(PlatformFixture.activePlatform());
        SubscriptionPlan plan = subscriptionPlanRepository.save(SubscriptionPlanFixture.activePlan(policy, platform));
        Party party = partyRepository.save(PartyFixture.activeParty(user.getId()));
        partyMemberRepository.save(PartyMemberFixture.createPartyMember(party, user));
        subscriptionRepository.save(SubscriptionFixture.activePlan(plan, party));

        String inviteCode = party.getInviteCode();

        // when & then
        assertThatThrownBy(() -> partyFindByCodeBusiness.get(inviteCode))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_FOUND.getMessage());
    }
}
