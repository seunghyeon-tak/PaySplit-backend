package com.paysplit.api.business.user;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.UserLoginBusiness;
import com.paysplit.api.business.UserRegisterBusiness;
import com.paysplit.api.dto.auth.request.LoginRequest;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.api.dto.auth.response.RegisterResponse;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.common.jwt.RefreshTokenService;
import com.paysplit.db.repository.UserAuthRepository;
import com.paysplit.db.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserLoginIntegrationTest {
    private static final String TEST_PASSWORD = "testPassword1234!";
    private static final String TEST_VALID_PASSWORD = "validTestPassword!";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserLoginBusiness userLoginBusiness;

    @Autowired
    private UserRegisterBusiness userRegisterBusiness;

    @AfterEach
    void testDown() {
        userAuthRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인시 token이 정상 발급")
    void create_token_integration() {
        // given
        RegisterResponse registerResponse = userRegisterBusiness.create(RegisterRequest.builder()
                .name("test0099")
                .email("test0099@test.com")
                .password(TEST_PASSWORD)
                .build());

        LoginRequest request = LoginRequest.builder()
                .email("test0099@test.com")
                .password(TEST_PASSWORD)
                .build();

        // when
        LoginResponse response = userLoginBusiness.login(request);

        // then
        String refreshToken = refreshTokenService.getRefreshToken(registerResponse.getUserId());
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 UserException 발생")
    void login_exception_userNotFound() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("test0098@test.com")
                .password(TEST_PASSWORD)
                .build();

        // when & then
        assertThatThrownBy(() -> userLoginBusiness.login(request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.EMAIL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("비밀번호 불일치 시 UserAuthExcetion 발생")
    void login_exception_invalidPassword() {
        // given
        userRegisterBusiness.create(RegisterRequest.builder()
                .name("test0099")
                .email("test0099@test.com")
                .password(TEST_PASSWORD)
                .build());

        LoginRequest request = LoginRequest.builder()
                .email("test0099@test.com")
                .password(TEST_VALID_PASSWORD)
                .build();

        // when & then
        assertThatThrownBy(() -> userLoginBusiness.login(request))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_PASSWORD.getMessage());
    }
}
