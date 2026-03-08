package com.paysplit.api.dto.party.response;

import com.paysplit.db.enums.PartyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyCreateResponse {
    private Long partyId;
    private PartyStatus status;
    private String inviteCode;
}
