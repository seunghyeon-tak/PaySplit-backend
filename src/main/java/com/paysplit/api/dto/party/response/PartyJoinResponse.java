package com.paysplit.api.dto.party.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyJoinResponse {
    Long partyId;
    Long planId;
    String planName;
}
