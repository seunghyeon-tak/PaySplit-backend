package com.paysplit.api.business;

import com.paysplit.api.converter.UserRegisterConverter;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.response.RegisterResponse;
import com.paysplit.api.service.UserAuthService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.annotation.Business;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.UserAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Business
@RequiredArgsConstructor
@Transactional
public class UserRegisterBusiness {
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final UserRegisterConverter userRegisterConverter;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse create(RegisterRequest request) {
        String email = request.getEmail();
        String name = request.getName();
        String password = request.getPassword();

        // 이메일 중복 확인
        userService.validateEmail(email);

        // 비밀번호 암호화 (BCrypt)
        String encodedPassword = passwordEncoder.encode(password);

        // User 저장
        User user = userService.createUser(name, email);

        // UserAuth 저장 (Local provider)
        userAuthService.createUserAuth(user, UserAuthProvider.LOCAL, email, encodedPassword);

        // 응답 반환
        return userRegisterConverter.toResponse(user);
    }
}
