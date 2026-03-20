package com.paysplit.api.business.party;

import com.paysplit.api.business.PartyInviteJoinBusiness;
import com.paysplit.api.converter.PartyInviteJoinConverter;
import com.paysplit.api.dto.party.request.PartyJoinRequest;
import com.paysplit.api.dto.party.response.PartyJoinResponse;
import com.paysplit.api.service.*;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartyInviteJoinBusinessTest {
    @Mock
    private UserService userService;

    @Mock
    private PartyService partyService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    @Mock
    private PartyMemberService partyMemberService;

    @Mock
    private PartyInviteJoinConverter partyInviteJoinConverter;

    @InjectMocks
    private PartyInviteJoinBusiness partyInviteJoinBusiness;

    @Test
    @DisplayName("초대 코드로 파티에 정상 가입 하였습니다")
    void join_success() {
        // given
        Long userId = 1L;
        String inviteCode = "TESTJOINCODE";

        PartyJoinRequest request = PartyJoinRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);
        Party party = mock(Party.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        PartyJoinResponse response = mock(PartyJoinResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getPartyByInviteCode(inviteCode)).thenReturn(party);
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscriptionPlanService.getById(subscription.getPlanId())).thenReturn(plan);
        when(partyInviteJoinConverter.toResponse(party.getId(), plan.getId(), plan.getName())).thenReturn(response);

        // when
        PartyJoinResponse result = partyInviteJoinBusiness.join(inviteCode, request);

        // then
        verify(userService).getById(userId);
        verify(partyService).getPartyByInviteCode(inviteCode);
        verify(subscriptionService).getByPartyId(party);
        verify(subscriptionPlanService).getById(subscription.getPlanId());
        verify(partyMemberService).validatePartyNotFull(anyInt(), anyInt());
        verify(partyMemberService).validateNotAlreadyJoined(anyLong(), anyLong());
        verify(partyMemberService).createPartyMember(party, user);

        assertThat(result).isEqualTo(response);
    }
}
