package com.paysplit.api.converter;

import com.paysplit.api.dto.auth.response.RegisterResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.User;

@Converter
public class UserRegisterConverter {
    public RegisterResponse toResponse(User user) {
        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }
}
