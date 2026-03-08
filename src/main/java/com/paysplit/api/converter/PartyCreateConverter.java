package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.Party;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class PartyCreateConverter {
    public PartyCreateResponse toResponse(Party party) {
        return PartyCreateResponse.builder()
                .partyId(party.getId())
                .status(party.getStatus())
                .inviteCode(party.getInviteCode())
                .build();
    }
}
