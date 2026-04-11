package com.paysplit.api.converter;

import com.paysplit.api.dto.party.response.PartyLeaveResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.LeaveStatus;

@Converter
public class PartyLeaveConverter {
    public PartyLeaveResponse toResponse(User user, Party party, Subscription subscription, PartyMember partyMember, LeaveStatus leaveStatus) {
        return PartyLeaveResponse.builder()
                .userId(user.getId())
                .partyId(party.getId())
                .planName(subscription.getPlan().getName())
                .leaveDate(subscription.getEndedAt())
                .status(leaveStatus)
                .build();
    }
}
