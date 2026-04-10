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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

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
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        User user;
        if ("kakao".equals(registrationId)) {
            user = handlerKakao(oAuth2User);
        } else {
            user = handlerGoogle(oAuth2User);
        }

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

    private User handlerGoogle(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .build()
                ));

        userAuthRepository.findByUserAndProvider(user, UserAuthProvider.GOOGLE)
                .orElseGet(() -> userAuthRepository.save(
                        UserAuth.builder()
                                .user(user)
                                .provider(UserAuthProvider.GOOGLE)
                                .identifier(email)
                                .build()
                ));
        return user;
    }

    private User handlerKakao(OAuth2User oAuth2User) {
        String kakaoId = oAuth2User.getName();
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        // UserAuth로 기존 카카오 유저 조회
        return userAuthRepository.findByProviderAndIdentifier(UserAuthProvider.KAKAO, kakaoId)
                .map(UserAuth::getUser)
                .orElseGet(() -> {
                    User newUser = userRepository.save(
                            User.builder()
                                    .name(nickname)
                                    .build()
                    );
                    userAuthRepository.save(
                            UserAuth.builder()
                                    .user(newUser)
                                    .provider(UserAuthProvider.KAKAO)
                                    .identifier(kakaoId)
                                    .build()
                    );

                    return newUser;
                });
    }
}
