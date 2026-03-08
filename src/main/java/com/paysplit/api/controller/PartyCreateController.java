package com.paysplit.api.controller;

import com.paysplit.api.business.PartyCreateBusiness;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyCreateController {
    private final PartyCreateBusiness partyCreateBusiness;

    @PostMapping
    public ApiResponse<PartyCreateResponse> create(@Valid @RequestBody PartyCreateRequest request) {
        PartyCreateResponse response = partyCreateBusiness.create(request);

        return ApiResponse.success(response);
    }
}
