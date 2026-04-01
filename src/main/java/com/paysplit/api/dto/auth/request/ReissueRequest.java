package com.paysplit.api.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReissueRequest {
    @Schema(name = "refresh_token", description = "리프레쉬 토큰", example = "askdjaksjflkajsfklaj")
    @NotNull
    private String refreshToken;
}
