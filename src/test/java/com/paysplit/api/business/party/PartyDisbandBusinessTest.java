package com.paysplit.api.business.party;

import com.paysplit.api.business.PartyDisbandBusiness;
import com.paysplit.api.converter.PartyDisbandConverter;
import com.paysplit.api.dto.party.response.PartyDisbandResponse;
import com.paysplit.api.service.PartyMemberService;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.SubscriptionService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.error.subscription.SubscriptionErrorCode;
import com.paysplit.common.error.subscription.SubscriptionException;
import com.paysplit.common.util.SecurityUtils;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.SubscriptionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartyDisbandBusinessTest {
    @Mock
    private UserService userService;

    @Mock
    private PartyService partyService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PartyMemberService partyMemberService;

    @Mock
    private PartyDisbandConverter partyDisbandConverter;

    @InjectMocks
    private PartyDisbandBusiness partyDisbandBusiness;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("파티 해산 성공 (PENDING) - 즉시 해산")
    void disband_success_immediate() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long partyId = 1L;

        User user = mock(User.class);
        Party party = mock(Party.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        PartyDisbandResponse response = mock(PartyDisbandResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(subscription.getPlan()).thenReturn(plan);
        when(plan.getName()).thenReturn("넷플릭스 스탠다드");
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.PENDING);
        when(partyDisbandConverter.toResponse(eq(partyId), any(), any())).thenReturn(response);

        // when
        PartyDisbandResponse result = partyDisbandBusiness.disband(partyId, userId);

        // then
        verify(partyService).getById(partyId);
        verify(partyService).validatePartyLeader(partyId, userId);
        verify(subscriptionService).getByPartyId(party);
        verify(partyMemberService).leaveAllPartyMembers(partyId);
        verify(subscriptionService).subscriptionCancel(subscription.getId());
        verify(partyService).partyDisband(partyId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("파티 해산 성공 (ACTIVE) - 해산 예약")
    void disband_success_reserved() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long partyId = 1L;

        User user = mock(User.class);
        Party party = mock(Party.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        PartyDisbandResponse response = mock(PartyDisbandResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(subscription.getPlan()).thenReturn(plan);
        when(plan.getName()).thenReturn("넷플릭스 스탠다드");
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(partyDisbandConverter.toResponse(eq(partyId), any(), any())).thenReturn(response);

        // when
        PartyDisbandResponse result = partyDisbandBusiness.disband(partyId, userId);

        // then
        verify(partyService).getById(partyId);
        verify(partyService).validatePartyLeader(partyId, userId);
        verify(subscriptionService).getByPartyId(party);
        verify(partyService).requestDisband(partyId);

        verify(partyMemberService, never()).leaveAllPartyMembers(any());
        verify(subscriptionService, never()).subscriptionCancel(any());
        verify(partyService, never()).partyDisband(any());

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("구독 상태 PENDING/ACTIVE 아닐때 예외")
    void disband_exception_subscriptionCancelFailed() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long partyId = 1L;

        User user = mock(User.class);
        Party party = mock(Party.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.getById(partyId)).thenReturn(party);
        when(subscriptionService.getByPartyId(party)).thenReturn(subscription);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.EXPIRED);

        // when & then
        assertThatThrownBy(() -> partyDisbandBusiness.disband(partyId, userId))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining(SubscriptionErrorCode.SUBSCRIPTION_CANCEL_FAILED.getMessage());

        verify(partyService).getById(partyId);
        verify(partyService).validatePartyLeader(partyId, userId);
        verify(subscriptionService).getByPartyId(party);

        verify(partyMemberService, never()).leaveAllPartyMembers(any());
        verify(subscriptionService, never()).subscriptionCancel(any());
        verify(partyService, never()).partyDisband(any());
        verify(partyService, never()).requestDisband(partyId);
    }
}
