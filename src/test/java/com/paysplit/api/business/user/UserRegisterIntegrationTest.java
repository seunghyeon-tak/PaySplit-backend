package com.paysplit.api.business.user;

import com.paysplit.PaysplitApplication;
import com.paysplit.api.business.UserRegisterBusiness;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.response.RegisterResponse;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.repository.UserAuthRepository;
import com.paysplit.db.repository.UserRepository;
import com.paysplit.support.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PaysplitApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRegisterIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private UserRegisterBusiness userRegisterBusiness;

    @AfterEach
    void testDown() {
        userAuthRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입시 User와 UserAuth가 저장된다.")
    void create_user_userAuth_integration() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .name("test0099")
                .email("test0099@test.com")
                .password("1234!")
                .build();

        // when
        RegisterResponse response = userRegisterBusiness.create(request);

        // then
        Optional<User> user = userRepository.findById(response.getUserId());
        Optional<UserAuth> userAuth = userAuthRepository.findByUser(user.get());
        assertThat(response.getUserId()).isEqualTo(user.get().getId());
        assertThat(response.getEmail()).isEqualTo(user.get().getEmail());
        assertThat(userAuth).isPresent();
    }

    @Test
    @DisplayName("회원가입 시 중복 이메일로인해 UserException이 발생")
    void create_exception_validateEmail() {
        // given
        User user = userRepository.save(UserFixture.activeUser());

        RegisterRequest request = RegisterRequest.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password("123123!")
                .build();

        // when & then
        assertThatThrownBy(() -> userRegisterBusiness.create(request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.DUPLICATE_EMAIL.getMessage());
    }
}
