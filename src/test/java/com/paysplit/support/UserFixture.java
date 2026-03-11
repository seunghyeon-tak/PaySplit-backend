package com.paysplit.support;

import com.paysplit.db.domain.User;

import java.time.LocalDateTime;

public class UserFixture {
    public static User activeUser() {
        return User.builder()
                .name("tester01")
                .email("tester@test.com")
                .build();
    }

    public static User withdrawnUser() {
        return User.builder()
                .name("withdrawn01")
                .email("withdrawn@test.com")
                .withdrawalAt(LocalDateTime.now())
                .build();
    }
}
