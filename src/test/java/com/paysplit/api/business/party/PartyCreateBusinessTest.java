package com.paysplit.api.business.party;

import com.paysplit.api.business.PartyCreateBusiness;
import com.paysplit.api.converter.PartyCreateConverter;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.api.service.*;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.subscriptionplan.SubscriptionPlanErrorCode;
import com.paysplit.common.error.subscriptionplan.SubscriptionPlanException;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.common.util.SecurityUtils;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyCreateBusinessTest {
    @Mock
    private PartyService partyService;

    @Mock
    private UserService userService;

    @Mock
    private PartyMemberService partyMemberService;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PartyCreateConverter partyCreateConverter;

    @Mock
    private WaitingQueueService waitingQueueService;

    @InjectMocks
    private PartyCreateBusiness partyCreateBusiness;

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
    @DisplayName("파티 생성이 정상적으로 실행된다.")
    void create_party_success() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(planId)
                .build();

        Party party = mock(Party.class);
        User user = mock(User.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        Subscription subscription = mock(Subscription.class);
        PartyCreateResponse response = mock(PartyCreateResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionPlanService.getById(planId)).thenReturn(plan);
        when(partyService.existInviteCode(any())).thenReturn(false);
        when(partyService.createParty(eq(userId), any(String.class))).thenReturn(party);
        when(subscriptionService.createSubscription(plan, party)).thenReturn(subscription);
        when(waitingQueueService.isInWaitingQueue(any(), any())).thenReturn(false);
        when(waitingQueueService.getWaitingQueueSize(any())).thenReturn(0L);
        when(partyCreateConverter.toResponse(party, subscription)).thenReturn(response);

        // when
        PartyCreateResponse result = partyCreateBusiness.create(userId, request);

        // then
        verify(userService).validateNotWithdrawn(user);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(partyService).createParty(eq(userId), codeCaptor.capture());
        verify(partyMemberService).createPartyMember(party, user);
        verify(subscriptionService).createSubscription(plan, party);
        verify(waitingQueueService).isInWaitingQueue(planId, userId);

        String capturedCode = codeCaptor.getValue();
        assertThat(capturedCode).hasSize(8);
        assertThat(capturedCode).isUpperCase();
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("존재하지 않는 유저가 파티 생성시 UserException이 발생한다.")
    void create_party_whenGetById_throwException() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(planId)
                .build();

        when(userService.getById(userId)).thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(userId, request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());

        verify(partyService, never()).createParty(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }

    @Test
    @DisplayName("탈퇴한 유저가 파티 생성 시 UserException이 발생한다.")
    void create_party_whenWithdrawnUser_throwException() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(planId)
                .build();

        User user = mock(User.class);

        when(userService.getById(userId)).thenReturn(user);
        doThrow(new UserException(UserErrorCode.LEFT_USER))
                .when(userService).validateNotWithdrawn(user);

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(userId, request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.LEFT_USER.getMessage());

        verify(partyService, never()).createParty(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }

    @Test
    @DisplayName("플랜 존재 확인시 구독 플랜 정보 찾을 수 없을때 SubscriptionException이 발생한다.")
    void create_party_getById_throwException() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(planId)
                .build();

        User user = mock(User.class);

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionPlanService.getById(planId))
                .thenThrow(new SubscriptionPlanException(SubscriptionPlanErrorCode.PLAN_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(userId, request))
                .isInstanceOf(SubscriptionPlanException.class)
                .hasMessageContaining(SubscriptionPlanErrorCode.PLAN_NOT_FOUND.getMessage());

        verify(partyService, never()).createParty(any(), any());
        verify(subscriptionService, never()).createSubscription(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }

    @Test
    @DisplayName("초대코드 3회 충돌시 PartyException이 발생한다.")
    void create_party_generateUniqueInviteCode_throwException() {
        // given
        Long userId = SecurityUtils.getCurrentUserId();
        Long planId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .planId(planId)
                .build();

        User user = mock(User.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.existInviteCode(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(userId, request))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.INVITE_CODE_GENERATE_FAILED.getMessage());

        verify(partyService, times(3)).existInviteCode(any());
        verify(partyService, never()).createParty(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }
}
