package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyAutoMatchResponse;
import com.paysplit.common.annotation.Converter;

@Converter
public class PartyAutoMatchConverter {
    public PartyAutoMatchResponse toResponse(String status, Long partyId, Long planId, String planName) {
        return PartyAutoMatchResponse.builder()
                .status(status)
                .partyId(partyId)
                .planId(planId)
                .planName(planName)
                .build();
    }
}
