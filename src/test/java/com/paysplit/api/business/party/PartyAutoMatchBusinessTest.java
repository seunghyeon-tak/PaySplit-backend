package com.paysplit.api.business.party;

import com.paysplit.api.business.PartyAutoMatchBusiness;
import com.paysplit.api.converter.PartyAutoMatchConverter;
import com.paysplit.api.dto.party.request.PartyAutoMatchRequest;
import com.paysplit.api.dto.party.response.PartyAutoMatchResponse;
import com.paysplit.api.service.*;
import com.paysplit.common.util.SecurityUtils;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartyAutoMatchBusinessTest {
    @Mock
    private UserService userService;

    @Mock
    private PartyMemberService partyMemberService;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    @Mock
    private PartyService partyService;

    @Mock
    private WaitingQueueService waitingQueueService;

    @Mock
    private PartyAutoMatchConverter partyAutoMatchConverter;

    @InjectMocks
    private PartyAutoMatchBusiness partyAutoMatchBusiness;

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
    @DisplayName("파티 자동 매칭이 정상적으로 실행된다. - WAITING")
    void waiting_match_party_success() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(planId)
                .build();

        User user = mock(User.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        PartyAutoMatchResponse response = mock(PartyAutoMatchResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionPlanService.getById(planId)).thenReturn(plan);
        when(partyService.findAvailableParty(planId)).thenReturn(Optional.empty());
        when(partyAutoMatchConverter.toResponse("WAITING", null, plan.getId(), plan.getName())).thenReturn(response);

        // when
        PartyAutoMatchResponse result = partyAutoMatchBusiness.auto(userId, request);

        // then
        verify(userService).getById(userId);
        verify(subscriptionPlanService).getById(planId);
        verify(partyService).findAvailableParty(planId);
        verify(waitingQueueService).addToWaitingQueue(planId, userId);
        verify(partyMemberService, never()).createPartyMember(any(), any());

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("파티 자동 매칭이 정상적으로 실행된다. - JOINED")
    void joined_match_party_success() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyAutoMatchRequest request = PartyAutoMatchRequest.builder()
                .planId(planId)
                .build();

        User user = mock(User.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        Party party = mock(Party.class);
        PartyAutoMatchResponse response = mock(PartyAutoMatchResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionPlanService.getById(planId)).thenReturn(plan);
        when(partyService.findAvailableParty(planId)).thenReturn(Optional.of(party));
        when(partyAutoMatchConverter.toResponse("JOINED", party.getId(), plan.getId(), plan.getName())).thenReturn(response);

        // when
        PartyAutoMatchResponse result = partyAutoMatchBusiness.auto(userId, request);

        // then
        verify(userService).getById(userId);
        verify(subscriptionPlanService).getById(planId);
        verify(partyService).findAvailableParty(planId);
        verify(partyMemberService).createPartyMember(party, user);
        verify(waitingQueueService, never()).addToWaitingQueue(anyLong(), anyLong());

        assertThat(result).isEqualTo(response);
    }
}
