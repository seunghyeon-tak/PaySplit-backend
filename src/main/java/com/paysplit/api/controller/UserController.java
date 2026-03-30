package com.paysplit.api.controller;

import com.paysplit.api.business.ReissueBusiness;
import com.paysplit.api.business.UserLoginBusiness;
import com.paysplit.api.business.UserLogoutBusiness;
import com.paysplit.api.business.UserRegisterBusiness;
import com.paysplit.api.dto.auth.request.LoginRequest;
import com.paysplit.api.dto.auth.request.RegisterRequest;
import com.paysplit.api.dto.auth.request.ReissueRequest;
import com.paysplit.api.dto.auth.response.LoginResponse;
import com.paysplit.api.dto.auth.response.RegisterResponse;
import com.paysplit.api.dto.auth.response.ReissueResponse;
import com.paysplit.api.response.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserRegisterBusiness userRegisterBusiness;
    private final UserLoginBusiness userLoginBusiness;
    private final ReissueBusiness reissueBusiness;
    private final UserLogoutBusiness userLogoutBusiness;

    @PostMapping("/register")
    public ApiResult<RegisterResponse> create(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userRegisterBusiness.create(request);
        return ApiResult.success(response);
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userLoginBusiness.login(request);
        return ApiResult.success(response);
    }

    @PostMapping("/reissue")
    public ApiResult<ReissueResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        ReissueResponse response = reissueBusiness.reissue(request);
        return ApiResult.success(response);
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        userLogoutBusiness.logout(bearerToken);
        return ApiResult.success();
    }
}
