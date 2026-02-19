package com.paysplit.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    // 성공 - 데이터 없음
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "SUCCESS", null, null);
    }

    // 성공 - 데이터 있음
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", null, data);
    }

    // 실패 - 메시지만
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    // 실패 - 데이터 포함
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
