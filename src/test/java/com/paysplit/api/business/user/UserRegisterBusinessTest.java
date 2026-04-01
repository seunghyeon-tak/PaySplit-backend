package com.paysplit.api.business.user;

import com.paysplit.api.business.UserRegisterBusiness;
import com.paysplit.api.converter.UserRegisterConverter;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.response.RegisterResponse;
import com.paysplit.api.service.UserAuthService;
import com.paysplit.api.service.UserService;
import com.paysplit.db.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRegisterBusinessTest {
    @Mock
    private UserService userService;

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private UserRegisterConverter userRegisterConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegisterBusiness userRegisterBusiness;

    @Test
    @DisplayName("유저 회원가입 정상 실행된다")
    void user_register_success() {
        // given
        String email = "test@test.com";
        String name = "test";
        String password = "1234!";

        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        User user = mock(User.class);
        RegisterResponse response = mock(RegisterResponse.class);

        when(passwordEncoder.encode(eq(password))).thenReturn("encodePassword");
        when(userService.createUser(name, email)).thenReturn(user);
        when(userRegisterConverter.toResponse(user)).thenReturn(response);

        // when
        RegisterResponse result = userRegisterBusiness.create(request);

        // then
        verify(passwordEncoder).encode(password);
        verify(userService).createUser(name, email);

        assertThat(result).isEqualTo(response);
    }
}
