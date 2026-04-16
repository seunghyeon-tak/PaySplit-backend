package com.paysplit.api.dto.party.response;

import com.paysplit.db.enums.PartyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyDisbandResponse {
    @Schema(name = "party_id", description = "파티 ID", example = "1")
    private Long partyId;

    @Schema(name = "plan_name", description = "구독 플랜 이름", example = "넷플릭스 스탠다드")
    private String planName;

    @Schema(name = "disbanded_at", description = "해산 날짜", example = "2026-03-05")
    private LocalDateTime disbandedAt;

    @Schema(description = "파티 상태", example = "DISBANDED")
    private PartyStatus status;
}
