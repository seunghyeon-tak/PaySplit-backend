package com.paysplit.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAuthProvider {
    LOCAL("일반 로그인"),
    GOOGLE("구글 로그인"),
    KAKAO("카카오 로그인"),
    ;

    private final String description;
}
