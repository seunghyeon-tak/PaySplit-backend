package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyDisbandResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.enums.PartyStatus;

import java.time.LocalDateTime;

@Converter
public class PartyDisbandConverter {
    public PartyDisbandResponse toResponse(Long partyId, String planName, LocalDateTime disbandedAt) {
        return PartyDisbandResponse.builder()
                .partyId(partyId)
                .planName(planName)
                .disbandedAt(disbandedAt)
                .status(PartyStatus.DISBANDED)
                .build();
    }
}
