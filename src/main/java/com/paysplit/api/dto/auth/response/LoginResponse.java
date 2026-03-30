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
public class LoginResponse {
    @Schema(name = "access_token", description = "엑세스 토큰", example = "askflakjf")
    private String accessToken;

    @Schema(name = "refresh_token", description = "리프레쉬 토큰", example = "aslkfjlaksjfkl1")
    private String refreshToken;
}
