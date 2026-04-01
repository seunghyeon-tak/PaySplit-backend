package com.paysplit.api.business.user;

import com.paysplit.api.business.UserLoginBusiness;
import com.paysplit.api.converter.UserLoginConverter;
import com.paysplit.api.dto.auth.request.LoginRequest;
import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.api.service.UserAuthService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.jwt.RefreshTokenService;
import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.enums.UserAuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLoginBusinessTest {
    @Mock
    private UserService userService;

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserLoginConverter userLoginConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserLoginBusiness userLoginBusiness;

    @Test
    @DisplayName("유저 로그인이 정상적으로 실행")
    void user_login_success() {
        // given
        String email = "test0099@test.com";
        String password = "1234!";

        User user = mock(User.class);
        UserAuth userAuth = mock(UserAuth.class);
        LoginResponse response = mock(LoginResponse.class);

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        when(userService.getByEmail(email)).thenReturn(user);
        when(userAuthService.getByUserAndProvider(user, UserAuthProvider.LOCAL)).thenReturn(userAuth);
        when(userAuth.getPassword()).thenReturn("encodedPassword");
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(jwtProvider.generateAccessToken(eq(user.getId()))).thenReturn("askdjlaksjd");
        when(jwtProvider.generateRefreshToken(eq(user.getId()))).thenReturn("fkjfkjkjqk");
        when(userLoginConverter.toResponse(anyString(), anyString())).thenReturn(response);

        // when
        LoginResponse result = userLoginBusiness.login(request);

        // then
        verify(userService).getByEmail(email);
        verify(userAuthService).getByUserAndProvider(user, UserAuthProvider.LOCAL);
        verify(jwtProvider).generateAccessToken(anyLong());
        verify(jwtProvider).generateRefreshToken(anyLong());

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("유저 로그인 시 비밀번호 검증 실패")
    void user_login_exception_invalidPassword() {
        // given
        String email = "test0099@test.com";
        String password = "1234!";

        User user = mock(User.class);
        UserAuth userAuth = mock(UserAuth.class);

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        when(userService.getByEmail(email)).thenReturn(user);
        when(userAuthService.getByUserAndProvider(user, UserAuthProvider.LOCAL)).thenReturn(userAuth);
        when(userAuth.getPassword()).thenReturn("notMatchesPassword");
        when(passwordEncoder.matches(password, "notMatchesPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userLoginBusiness.login(request))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_PASSWORD.getMessage());

        verify(jwtProvider, never()).generateAccessToken(anyLong());
        verify(jwtProvider, never()).generateRefreshToken(anyLong());
    }
}
