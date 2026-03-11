package com.paysplit.api.party;

import com.paysplit.api.business.PartyCreateBusiness;
import com.paysplit.api.converter.PartyCreateConverter;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.api.service.PartyMemberService;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private PartyCreateConverter partyCreateConverter;

    @InjectMocks
    private PartyCreateBusiness partyCreateBusiness;

    @Test
    @DisplayName("파티 생성이 정상적으로 실행된다.")
    void create_party_success() {
        // given
        Long userId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .userId(userId)
                .build();

        Party party = mock(Party.class);
        User user = mock(User.class);
        PartyCreateResponse response = mock(PartyCreateResponse.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.existInviteCode(any())).thenReturn(false);
        when(partyService.createParty(eq(userId), any(String.class))).thenReturn(party);
        when(partyCreateConverter.toResponse(party)).thenReturn(response);

        // when
        PartyCreateResponse result = partyCreateBusiness.create(request);

        // then
        verify(userService).validateNotWithdrawn(user);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(partyService).createParty(eq(userId), codeCaptor.capture());
        verify(partyMemberService).createPartyMember(party, user);

        String capturedCode = codeCaptor.getValue();
        assertThat(capturedCode).hasSize(8);
        assertThat(capturedCode).isUpperCase();
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("존재하지 않는 유저가 파티 생성시 UserException이 발생한다.")
    void create_party_whenGetById_throwException() {
        // given
        Long userId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .userId(userId)
                .build();

        when(userService.getById(userId)).thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());

        verify(partyService, never()).createParty(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }

    @Test
    @DisplayName("탈퇴한 유저가 파티 생성 시 UserException이 발생한다.")
    void create_party_whenWithdrawnUser_throwException() {
        // given
        Long userId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);

        when(userService.getById(userId)).thenReturn(user);
        doThrow(new UserException(UserErrorCode.LEFT_USER))
                .when(userService).validateNotWithdrawn(user);

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.LEFT_USER.getMessage());

        verify(partyService, never()).createParty(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }

    @Test
    @DisplayName("초대코드 3회 충돌시 PartyException이 발생한다.")
    void create_party_generateUniqueInviteCode_throwException() {
        // given
        Long userId = 1L;

        PartyCreateRequest request = PartyCreateRequest.builder()
                .userId(userId)
                .build();

        User user = mock(User.class);

        when(userService.getById(userId)).thenReturn(user);
        when(partyService.existInviteCode(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> partyCreateBusiness.create(request))
                .isInstanceOf(PartyException.class)
                .hasMessageContaining(PartyErrorCode.INVITE_CODE_GENERATE_FAILED.getMessage());

        verify(partyService, times(3)).existInviteCode(any());
        verify(partyService, never()).createParty(any(), any());
        verify(partyMemberService, never()).createPartyMember(any(), any());
    }
}
