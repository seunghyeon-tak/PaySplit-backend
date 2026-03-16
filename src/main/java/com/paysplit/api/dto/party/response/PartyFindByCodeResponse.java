package com.paysplit.api.dto.party.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyFindByCodeResponse {
    Long partyId;
    String platformName;
    String leaderName;
    Integer currentMembers;
    Integer maxMembers;

}
