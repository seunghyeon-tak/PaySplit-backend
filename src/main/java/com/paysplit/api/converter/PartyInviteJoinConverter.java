package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyJoinResponse;
import com.paysplit.common.annotation.Converter;

@Converter
public class PartyInviteJoinConverter {
    public PartyJoinResponse toResponse(Long partyId, Long planId, String planName) {
        return PartyJoinResponse.builder()
                .partyId(partyId)
                .planId(planId)
                .planName(planName)
                .build();
    }
}
