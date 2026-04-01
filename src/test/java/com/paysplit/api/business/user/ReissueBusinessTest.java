package com.paysplit.api.business.user;

import com.paysplit.api.business.ReissueBusiness;
import com.paysplit.api.converter.ReissueConverter;
import com.paysplit.api.dto.auth.request.ReissueRequest;
import com.paysplit.api.dto.auth.response.ReissueResponse;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.jwt.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReissueBusinessTest {
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ReissueConverter reissueConverter;

    @InjectMocks
    private ReissueBusiness reissueBusiness;

    @Test
    @DisplayName("토큰 재발급 정상적으로 실행")
    void reissue_success() {
        // given
        String refreshToken = "alskdjalks";

        ReissueRequest request = ReissueRequest.builder()
                .refreshToken(refreshToken)
                .build();

        ReissueResponse response = mock(ReissueResponse.class);

        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserId(eq(refreshToken))).thenReturn(1L);
        when(refreshTokenService.getRefreshToken(anyLong())).thenReturn(refreshToken);
        when(jwtProvider.generateAccessToken(anyLong())).thenReturn("newAccessToken");
        when(reissueConverter.toResponse(anyString())).thenReturn(response);

        // when
        ReissueResponse result = reissueBusiness.reissue(request);

        // then
        verify(jwtProvider).validateToken(refreshToken);
        verify(jwtProvider).getUserId(refreshToken);
        verify(refreshTokenService).getRefreshToken(anyLong());
        verify(jwtProvider).generateAccessToken(anyLong());

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("유효하지 않은 리프레쉬 토큰 예외")
    void reissue_exception_invalidRefreshToken() {
        // given
        String refreshToken = "alskdjalks";

        ReissueRequest request = ReissueRequest.builder()
                .refreshToken(refreshToken)
                .build();

        when(jwtProvider.validateToken(refreshToken)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reissueBusiness.reissue(request))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());

        verify(jwtProvider, never()).getUserId(anyString());
        verify(refreshTokenService, never()).getRefreshToken(anyLong());
        verify(jwtProvider, never()).generateAccessToken(anyLong());
    }

    @Test
    @DisplayName("Redis에 저장된 토큰과 불일치 예외")
    void reissue_exception_tokenMismatch() {
        // given
        String refreshToken = "alskdjalks";

        ReissueRequest request = ReissueRequest.builder()
                .refreshToken(refreshToken)
                .build();

        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserId(eq(refreshToken))).thenReturn(1L);
        when(refreshTokenService.getRefreshToken(anyLong())).thenReturn("failToken");

        // when & then
        assertThatThrownBy(() -> reissueBusiness.reissue(request))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());

        verify(jwtProvider, never()).generateAccessToken(anyLong());
    }
}
