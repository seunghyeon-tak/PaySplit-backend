package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;

@Converter
public class PartyCreateConverter {
    public PartyCreateResponse toResponse(Party party, Subscription subscription) {
        return PartyCreateResponse.builder()
                .partyId(party.getId())
                .planId(subscription.getPlanId())
                .status(party.getStatus())
                .inviteCode(party.getInviteCode())
                .build();
    }
}
