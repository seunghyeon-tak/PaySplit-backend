package com.paysplit.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResult<T> {
    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 코드", example = "SUCCESS")
    private final String code;

    @Schema(description = "응답 메시지", example = "성공")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    // 성공 - 데이터 없음
    public static ApiResult<Void> success() {
        return new ApiResult<>(true, "SUCCESS", null, null);
    }

    // 성공 - 데이터 있음
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, "SUCCESS", null, data);
    }

    // 실패 - 메시지만
    public static ApiResult<Void> error(String code, String message) {
        return new ApiResult<>(false, code, message, null);
    }

    // 실패 - 데이터 포함
    public static <T> ApiResult<T> error(String code, String message, T data) {
        return new ApiResult<>(false, code, message, data);
    }
}
