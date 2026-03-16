package com.paysplit.api.business.party;

import com.paysplit.api.business.PartyFindByCodeBusiness;
import com.paysplit.api.converter.PartyFindByCodeConverter;
import com.paysplit.api.dto.party.response.PartyFindByCodeResponse;
import com.paysplit.api.service.*;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.subscription.SubscriptionErrorCode;
import com.paysplit.common.error.subscription.SubscriptionException;
import com.paysplit.common.error.subscriptionplan.SubscriptionPlanErrorCode;
import com.paysplit.common.error.subscriptionplan.SubscriptionPlanException;
import com.paysplit.db.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartyFindByCodeBusinessTest {
    @Mock
    private PartyService partyService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    @Mock
    private UserService userService;

    @Mock
    private PartyMemberService partyMemberService;

    @Mock
    private PartyFindByCodeConverter partyFindByCodeConverter;

    @InjectMocks
    private PartyFindByCodeBusiness partyFindByCodeBusiness;

    @Test
    @DisplayName("파티 조회가 정상적으로 실행된다.")
    void get_success() {
        // given
        String inviteCode = "TESTPARTYWELCOM01";

        Party party = mock(Party.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        Platform platform = mock(Platform.class);
        User user = mock(User.class);
        PartyFindByCodeResponse response = mock(PartyFindByCodeResponse.class);

        when(partyService.getPartyByInviteCode(inviteCode)).thenReturn(party);
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscriptionPlanService.getById(subscription.getPlanId())).thenReturn(plan);
        when(plan.getPlatform()).thenReturn(platform);
        when(platform.getName()).thenReturn("Netflix");
        when(plan.getMaxMembers()).thenReturn(4);
        when(userService.getById(anyLong())).thenReturn(user);
        when(partyMemberService.countActiveMembers(anyLong())).thenReturn(3);
        when(partyFindByCodeConverter.toResponse(eq(party), any(), any(), anyInt(), anyInt())).thenReturn(response);

        // when
        PartyFindByCodeResponse result = partyFindByCodeBusiness.get(inviteCode);

        // then
        verify(partyService).getPartyByInviteCode(inviteCode);
        verify(subscriptionService).getByPartyId(party);
        verify(subscriptionPlanService).getById(subscription.getPlanId());
        verify(userService).getById(user.getId());
        verify(partyMemberService).countActiveMembers(party.getId());

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("파티가 초대 코드가 유효하지 않을때 PartyException이 발생한다.")
    void get_exception_invalidInviteCode() {
        // given
        String inviteCode = "TESTPARTYWELCOM01";

        when(partyService.getPartyByInviteCode(inviteCode))
                .thenThrow(new PartyException(PartyErrorCode.PARTY_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> partyFindByCodeBusiness.get(inviteCode))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_NOT_FOUND.getMessage());

        verify(subscriptionService, never()).getByPartyId(any());
        verify(subscriptionPlanService, never()).getById(anyLong());
    }
}
