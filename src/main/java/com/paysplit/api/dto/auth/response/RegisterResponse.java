package com.paysplit.api.dto.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    @Schema(name = "user_id", description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "유저 이메일", example = "test@test.com")
    private String email;
}
