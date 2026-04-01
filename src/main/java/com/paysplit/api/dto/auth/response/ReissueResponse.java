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
public class ReissueResponse {
    @Schema(name = "access_token", description = "엑세스 토큰", example = "ksdjkfljalskfjlaksj")
    private String accessToken;
}
