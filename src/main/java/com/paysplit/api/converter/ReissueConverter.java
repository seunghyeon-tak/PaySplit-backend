package com.paysplit.api.converter;

import com.paysplit.api.dto.auth.response.ReissueResponse;
import com.paysplit.common.annotation.Converter;

@Converter
public class ReissueConverter {
    public ReissueResponse toResponse(String accessToken) {
        return ReissueResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
