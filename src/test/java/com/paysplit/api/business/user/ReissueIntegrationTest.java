package com.paysplit.api.business.user;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.ReissueBusiness;
import com.paysplit.api.business.UserLoginBusiness;
import com.paysplit.api.business.UserRegisterBusiness;
import com.paysplit.api.dto.auth.request.LoginRequest;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.request.ReissueRequest;
import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.api.dto.auth.response.ReissueResponse;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
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
public class ReissueIntegrationTest {

    @Autowired
    private ReissueBusiness reissueBusiness;

    @Autowired
    private UserRegisterBusiness userRegisterBusiness;

    @Autowired
    private UserLoginBusiness userLoginBusiness;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @AfterEach
    void testDown() {
        userAuthRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("토큰 정상 재발급")
    void reissue_success_integration() {
        // given
        userRegisterBusiness.create(RegisterRequest.builder()
                .name("test0099")
                .email("test0099@test.com")
                .password("1234!")
                .build());

        LoginResponse loginResponse = userLoginBusiness.login(LoginRequest.builder()
                .email("test0099@test.com")
                .password("1234!")
                .build());

        ReissueRequest request = ReissueRequest.builder()
                .refreshToken(loginResponse.getRefreshToken())
                .build();

        // when
        ReissueResponse response = reissueBusiness.reissue(request);

        // then
        assertThat(response.getAccessToken()).isNotNull();
    }

    @Test
    @DisplayName("리프레쉬 토큰 유효성 검증 실패")
    void reissue_exception_invalidRefreshToken() {
        // given
        userRegisterBusiness.create(RegisterRequest.builder()
                .name("test0099")
                .email("test0099@test.com")
                .password("1234!")
                .build());

        userLoginBusiness.login(LoginRequest.builder()
                .email("test0099@test.com")
                .password("1234!")
                .build());

        ReissueRequest request = ReissueRequest.builder()
                .refreshToken("aslkdjlaksjdlk")
                .build();

        // when & then
        assertThatThrownBy(() -> reissueBusiness.reissue(request))
                .isInstanceOf(UserAuthException.class)
                .hasMessageContaining(UserAuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
    }
}
