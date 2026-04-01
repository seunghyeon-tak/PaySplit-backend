package com.paysplit.api.converter;

import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.common.annotation.Converter;

@Converter
public class UserLoginConverter {
    public LoginResponse toResponse(String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
