package com.paysplit.support;

import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.enums.UserAuthProvider;

public class UserAuthFixture {
    public static UserAuth activeUserAuth(User user) {
        return UserAuth.builder()
                .user(user)
                .provider(UserAuthProvider.LOCAL)
                .identifier(user.getEmail())
                .password("1234!")
                .build();
    }
}
