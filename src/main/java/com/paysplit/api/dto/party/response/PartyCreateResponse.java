package com.paysplit.api.dto.party.response;

import com.paysplit.db.enums.PartyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyCreateResponse {
    @Schema(name = "party_id", description = "파티 ID", example = "1")
    private Long partyId;

    @Schema(name = "plan_id", description = "구독 플랜 ID", example = "1")
    private Long planId;

    @Schema(description = "파티 상태", example = "RECRUITING")
    private PartyStatus status;

    @Schema(name = "invite_code", description = "파티 초대 코드", example = "TESTCODE")
    private String inviteCode;
}
