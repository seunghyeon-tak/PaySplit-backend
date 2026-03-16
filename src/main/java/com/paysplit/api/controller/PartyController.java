package com.paysplit.api.controller;

import com.paysplit.api.business.PartyCreateBusiness;
import com.paysplit.api.business.PartyFindByCodeBusiness;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.request.PartyFindByCodeRequest;
import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.api.dto.party.response.PartyFindByCodeResponse;
import com.paysplit.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController {
    private final PartyCreateBusiness partyCreateBusiness;
    private final PartyFindByCodeBusiness partyFindByCodeBusiness;

    @PostMapping
    public ApiResponse<PartyCreateResponse> create(@Valid @RequestBody PartyCreateRequest request) {
        PartyCreateResponse response = partyCreateBusiness.create(request);

        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PartyFindByCodeResponse> codeGetParties(@RequestParam PartyFindByCodeRequest request) {
        PartyFindByCodeResponse response = partyFindByCodeBusiness.get(request);
        return ApiResponse.success(response);
    }
}
