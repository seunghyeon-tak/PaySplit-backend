package com.paysplit.api.business.user;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.UserLoginBusiness;
import com.paysplit.api.business.UserLogoutBusiness;
import com.paysplit.api.business.UserRegisterBusiness;
import com.paysplit.api.dto.auth.request.LoginRequest;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.api.dto.auth.response.RegisterResponse;
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
public class UserLogoutIntegrationTest {
    @Autowired
    private UserRegisterBusiness userRegisterBusiness;

    @Autowired
    private UserLoginBusiness userLoginBusiness;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private UserLogoutBusiness userLogoutBusiness;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @AfterEach
    void testDown() {
        userAuthRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 로그아웃")
    void logout_success_integration() {
        // given
        RegisterResponse registerResponse = userRegisterBusiness.create(RegisterRequest.builder()
                .name("test0099")
                .email("test0099@test.com")
                .password("1234!")
                .build());

        LoginResponse loginResponse = userLoginBusiness.login(LoginRequest.builder()
                .email("test0099@test.com")
                .password("1234!")
                .build());

        String bearerToken = "Bearer " + loginResponse.getAccessToken();

        // when & then
        userLogoutBusiness.logout(bearerToken);

        String savedToken = refreshTokenService.getRefreshToken(registerResponse.getUserId());
        assertThat(savedToken).isNull();
    }

    @Test
    @DisplayName("유효하지 않은 AccessToken 예외")
    void logout_exception_invalidAccessToken() {
        // given
        String bearerToken = "Bearer asdasd";

        // when & then
        assertThatThrownBy(() -> userLogoutBusiness.logout(bearerToken))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_ACCESS_TOKEN.getMessage());
    }
}
