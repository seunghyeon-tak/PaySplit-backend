package com.paysplit.db.repository;

import com.paysplit.db.domain.User;
import com.paysplit.db.domain.UserAuth;
import com.paysplit.db.enums.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByUser(User user);

    Optional<UserAuth> findByUserAndProvider(User user, UserAuthProvider provider);

    Optional<UserAuth> findByProviderAndIdentifier(UserAuthProvider provider, String identifier);
}
