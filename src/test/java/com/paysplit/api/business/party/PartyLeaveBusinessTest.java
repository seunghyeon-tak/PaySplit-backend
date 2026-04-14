package com.paysplit.api.business.party;

import com.paysplit.api.business.PartyLeaveBusiness;
import com.paysplit.api.converter.PartyLeaveConverter;
import com.paysplit.api.dto.party.request.PartyLeaveRequest;
import com.paysplit.api.dto.party.response.PartyLeaveResponse;
import com.paysplit.api.service.PartyMemberService;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.SubscriptionService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.LeaveStatus;
import com.paysplit.db.enums.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartyLeaveBusinessTest {
    @Mock
    private UserService userService;

    @Mock
    private PartyMemberService partyMemberService;

    @Mock
    private PartyService partyService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PartyLeaveConverter partyLeaveConverter;

    @InjectMocks
    private PartyLeaveBusiness partyLeaveBusiness;

    @Test
    @DisplayName("파티 탈퇴 성공 - 즉시 탈퇴")
    void leave_success_immediate() {
        // given
        Long partyId = 1L;
        Long userId = 1L;
        PartyLeaveRequest request = PartyLeaveRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);
        Party party = mock(Party.class);
        PartyMember partyMember = mock(PartyMember.class);
        Subscription subscription = mock(Subscription.class);
        PartyLeaveResponse response = mock(PartyLeaveResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(partyMemberService.getByPartyAndUser(party, user)).thenReturn(partyMember);
        when(party.getLeaderId()).thenReturn(2L);
        when(user.getId()).thenReturn(userId);
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.PENDING);
        when(partyLeaveConverter.toResponse(user, party, subscription, partyMember, LeaveStatus.IMMEDIATE)).thenReturn(response);

        // when
        PartyLeaveResponse result = partyLeaveBusiness.leave(partyId, request);

        // then
        verify(userService).getById(userId);
        verify(partyService).getById(partyId);
        verify(partyMemberService).getByPartyAndUser(party, user);
        verify(subscriptionService).getByPartyId(party);
        verify(partyMemberService).leavePartyMember(user, party);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("파티 탈퇴 성공 - 탈퇴 예약")
    void leave_success_reserved() {
        // given
        Long partyId = 1L;
        Long userId = 1L;
        PartyLeaveRequest request = PartyLeaveRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);
        Party party = mock(Party.class);
        PartyMember partyMember = mock(PartyMember.class);
        Subscription subscription = mock(Subscription.class);
        PartyLeaveResponse response = mock(PartyLeaveResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(partyMemberService.getByPartyAndUser(party, user)).thenReturn(partyMember);
        when(party.getLeaderId()).thenReturn(2L);
        when(user.getId()).thenReturn(userId);
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(partyLeaveConverter.toResponse(user, party, subscription, partyMember, LeaveStatus.RESERVED)).thenReturn(response);

        // when
        PartyLeaveResponse result = partyLeaveBusiness.leave(partyId, request);

        // then
        verify(userService).getById(userId);
        verify(partyService).getById(partyId);
        verify(partyMemberService).getByPartyAndUser(party, user);
        verify(subscriptionService).getByPartyId(party);
        verify(partyMemberService).requestLeavePartyMember(user, party);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("파티장은 탈퇴 불가일때 PartyException 발생")
    void leave_exception_leaderCannotLeave() {
        // given
        Long partyId = 1L;
        Long userId = 1L;
        PartyLeaveRequest request = PartyLeaveRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);
        Party party = mock(Party.class);
        PartyMember partyMember = mock(PartyMember.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(partyMemberService.getByPartyAndUser(party, user)).thenReturn(partyMember);
        when(party.getLeaderId()).thenReturn(userId);
        when(user.getId()).thenReturn(userId);

        // when & then
        assertThatThrownBy(() -> partyLeaveBusiness.leave(partyId, request))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_LEADER_LEAVE_FAILED.getMessage());

        verify(subscriptionService, never()).getByPartyId(any());
        verify(partyMemberService, never()).leavePartyMember(any(), any());
    }

    @Test
    @DisplayName("파티 탈퇴 실패했을때 PartyException 발생")
    void leave_exception_leaveFailed() {
        // given
        Long partyId = 1L;
        Long userId = 1L;
        PartyLeaveRequest request = PartyLeaveRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);
        Party party = mock(Party.class);
        PartyMember partyMember = mock(PartyMember.class);
        Subscription subscription = mock(Subscription.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(partyMemberService.getByPartyAndUser(party, user)).thenReturn(partyMember);
        when(party.getLeaderId()).thenReturn(2L);
        when(user.getId()).thenReturn(userId);
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.EXPIRED);

        // when & then
        assertThatThrownBy(() -> partyLeaveBusiness.leave(partyId, request))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.PARTY_LEAVE_FAILED.getMessage());

        verify(partyMemberService, never()).leavePartyMember(any(), any());
        verify(partyMemberService, never()).requestLeavePartyMember(any(), any());
    }
}
