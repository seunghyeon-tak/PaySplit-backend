package com.paysplit.api.business;

import com.paysplit.api.converter.ReissueConverter;
import com.paysplit.api.dto.auth.request.ReissueRequest;
import com.paysplit.api.dto.auth.response.ReissueResponse;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@Business
@RequiredArgsConstructor
public class ReissueBusiness {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final ReissueConverter reissueConverter;

    public ReissueResponse reissue(ReissueRequest request) {
        // 헤더에서 Refresh Token 추출
        String refreshToken = request.getRefreshToken();

        // Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new UserAuthException(UserAuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Redis에 저장된 Refresh Token과 비교
        Long userId = jwtProvider.getUserId(refreshToken);
        if (!refreshTokenService.getRefreshToken(userId).equals(refreshToken)) {
            throw new UserAuthException(UserAuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새 Access Token 발급
        String newAccessToken = jwtProvider.generateAccessToken(userId);

        // 응답 반환
        return reissueConverter.toResponse(newAccessToken);
    }
}
