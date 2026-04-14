package com.paysplit.api.dto.party.request;

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
public class PartyLeaveRequest {
    @Schema(name = "user_id", description = "유저 ID", example = "1")
    @NotNull
    private Long userId;
}
