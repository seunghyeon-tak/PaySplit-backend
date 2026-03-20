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
public class PartyJoinResponse {
    @Schema(name = "party_id", description = "파티 ID", examples = "1")
    Long partyId;

    @Schema(name = "plan_id", description = "구독 플랜 ID", examples = "1")
    Long planId;

    @Schema(name = "plan_name", description = "구독 플랜 이름", examples = "넷플릭스 스탠다드")
    String planName;
}
