package com.paysplit.common.oauth2;

import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.jwt.RefreshTokenService;
import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.enums.UserAuthProvider;
import com.paysplit.db.repository.UserAuthRepository;
import com.paysplit.db.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // User 조회 or 생성
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .build()
                ));

        // UserAuth(google) 조회 or 생성
        userAuthRepository.findByUserAndProvider(user, UserAuthProvider.GOOGLE)
                .orElseGet(() -> userAuthRepository.save(
                        UserAuth.builder()
                                .user(user)
                                .provider(UserAuthProvider.GOOGLE)
                                .identifier(email)
                                .build()
                ));

        // jwt 발급 + redis에 refreshToken 저장
        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        // 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"accessToken\" : \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}"
        );

    }
}
