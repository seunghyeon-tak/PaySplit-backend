package com.paysplit.api.business;

import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@Business
@RequiredArgsConstructor
public class UserLogoutBusiness {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public void logout(String bearerToken) {
        String accessToken = bearerToken.substring(7);

        // access token에서 userId 추출
        if (!jwtProvider.validateToken(accessToken)) {
            throw new UserAuthException(UserAuthErrorCode.INVALID_ACCESS_TOKEN);
        }
        Long userId = jwtProvider.getUserId(accessToken);

        // redis에서 refresh token 삭제
        refreshTokenService.deleteRefreshToken(userId);
    }
}
