package com.paysplit.api.dto.party.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyAutoMatchResponse {
    @Schema(description = "파티 참여 상태", example = "JOINED")
    private String status;

    @Schema(name = "party_id", description = "파티 ID", example = "1")
    private Long partyId;

    @Schema(name = "plan_id", description = "플랜 ID", example = "1")
    private Long planId;

    @Schema(name = "plan_name", description = "구독 플랜 이름", example = "넷플릭스 스탠다드")
    private String planName;
}
