package com.paysplit.api.service;

import com.paysplit.common.error.user_auth.UserAuthErrorCode;
import com.paysplit.common.error.user_auth.UserAuthException;
import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.enums.UserAuthProvider;
import com.paysplit.db.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.paysplit.common.error.user_auth.UserAuthErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserAuthRepository userAuthRepository;

    public UserAuth createUserAuth(User user, UserAuthProvider provider, String identifier, String password) {
        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .provider(provider)
                .identifier(identifier)
                .password(password)
                .build();

        return userAuthRepository.save(userAuth);
    }

    public UserAuth getByUserAndProvider(User user, UserAuthProvider provider) {
        return userAuthRepository.findByUserAndProvider(user, provider)
                .orElseThrow(() -> new UserAuthException(USER_AUTH_NOT_FOUND));
    }
}
