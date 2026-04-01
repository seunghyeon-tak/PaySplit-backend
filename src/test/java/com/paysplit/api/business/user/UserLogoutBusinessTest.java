package com.paysplit.api.business.user;

import com.paysplit.api.business.UserLogoutBusiness;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLogoutBusinessTest {
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserLogoutBusiness userLogoutBusiness;

    @Test
    @DisplayName("정상 로그아웃")
    void logout_success() {
        // given
        String bearerToken = "Bearer newAccessToken";
        String accessToken = bearerToken.substring(7);


        when(jwtProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtProvider.getUserId(accessToken)).thenReturn(1L);

        // when
        userLogoutBusiness.logout(bearerToken);

        // then
        verify(jwtProvider).validateToken(accessToken);
        verify(jwtProvider).getUserId(accessToken);
        verify(refreshTokenService).deleteRefreshToken(1L);
    }

    @Test
    @DisplayName("유효하지 않은 Access Token 예외")
    void accessToken_exception() {
        // given
        String bearerToken = "Bearer newAccessToken";
        String accessToken = bearerToken.substring(7);

        when(jwtProvider.validateToken(accessToken)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userLogoutBusiness.logout(bearerToken))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_ACCESS_TOKEN.getMessage());

        verify(jwtProvider, never()).getUserId(anyString());
        verify(refreshTokenService, never()).deleteRefreshToken(anyLong());
    }
}
