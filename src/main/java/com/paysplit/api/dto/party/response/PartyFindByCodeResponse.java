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
public class PartyFindByCodeResponse {
    @Schema(name = "party_id", description = "파티 ID", examples = "1")
    Long partyId;

    @Schema(name = "platform_name", description = "플랫폼 이름", examples = "넷플릭스")
    String platformName;

    @Schema(name = "leader_name", description = "파티장 이름", examples = "파티장1")
    String leaderName;

    @Schema(name = "current_members", description = "현재 파티원 인원", examples = "2")
    Integer currentMembers;

    @Schema(name = "max_members", description = "최대 파티원 인원", examples = "4")
    Integer maxMembers;

}
