package com.paysplit.api.business;

import com.paysplit.api.converter.UserLoginConverter;
import com.paysplit.api.dto.auth.request.LoginRequest;
import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.api.service.UserAuthService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.jwt.RefreshTokenService;
import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.enums.UserAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Business
@RequiredArgsConstructor
@Transactional
public class UserLoginBusiness {
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserLoginConverter userLoginConverter;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // 이메일로 UserAuth 조회
        User user = userService.getByEmail(email);
        UserAuth userAuth = userAuthService.getByUserAndProvider(user, UserAuthProvider.LOCAL);

        // 비밀번호 검증 (BCrypt)
        if (!passwordEncoder.matches(password, userAuth.getPassword())) {
            throw new UserAuthException(UserAuthErrorCode.INVALID_PASSWORD);
        }

        // Access Token 발급
        String accessToken = jwtProvider.generateAccessToken(user.getId());

        // Refresh Token 발급 + redis 저장
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        // 응답 반환
        return userLoginConverter.toResponse(accessToken, refreshToken);
    }
}
