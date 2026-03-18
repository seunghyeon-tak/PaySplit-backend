package com.paysplit.api.controller;

import com.paysplit.api.business.PartyInviteJoinBusiness;
import com.paysplit.api.business.PartyCreateBusiness;
import com.paysplit.api.business.PartyFindByCodeBusiness;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.request.PartyJoinRequest;
import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.api.dto.party.response.PartyFindByCodeResponse;
import com.paysplit.api.dto.party.response.PartyJoinResponse;
import com.paysplit.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController {
    private final PartyCreateBusiness partyCreateBusiness;
    private final PartyFindByCodeBusiness partyFindByCodeBusiness;
    private final PartyInviteJoinBusiness partyInviteJoinBusiness;

    @PostMapping
    public ApiResponse<PartyCreateResponse> create(@Valid @RequestBody PartyCreateRequest request) {
        PartyCreateResponse response = partyCreateBusiness.create(request);

        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PartyFindByCodeResponse> codeGetParties(@RequestParam String inviteCode) {
        PartyFindByCodeResponse response = partyFindByCodeBusiness.get(inviteCode);
        return ApiResponse.success(response);
    }

    @PostMapping("/join")
    public ApiResponse<PartyJoinResponse> joinPartyMember(@RequestParam String inviteCode, @Valid @RequestBody PartyJoinRequest request) {
        PartyJoinResponse response = partyInviteJoinBusiness.join(inviteCode, request);
        return ApiResponse.success(response);
    }
}
