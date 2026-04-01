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
public class RegisterRequest {

    @Schema(description = "유저 이름", example = "김유저")
    @NotNull
    private String name;

    @Schema(description = "유저 이메일", example = "test@test.com")
    @NotNull
    private String email;

    @Schema(description = "유저 비밀번호", example = "1234qwer")
    @NotNull
    private String password;
}
