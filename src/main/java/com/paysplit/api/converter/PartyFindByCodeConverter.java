package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyFindByCodeResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.Party;
import lombok.RequiredArgsConstructor;

@Converter
public class PartyFindByCodeConverter {
    public PartyFindByCodeResponse toResponse(
            Party party, String platformName, String leaderName, int currentMembers, int maxMembers
    ) {
        return PartyFindByCodeResponse.builder()
                .partyId(party.getId())
                .platformName(platformName)
                .leaderName(leaderName)
                .currentMembers(currentMembers)
                .maxMembers(maxMembers)
                .build();
    }
}
