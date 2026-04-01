package com.paysplit.api.service;

import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.User;
import com.paysplit.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.paysplit.common.error.user.UserErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }

    public void validateNotWithdrawn(User user) {
        if (user.getWithdrawalAt() != null) {
            throw new UserException(LEFT_USER);
        }
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(EMAIL_NOT_FOUND));
    }

    public void validateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserException(DUPLICATE_EMAIL);
        }
    }

    public User createUser(String name, String email) {
        User user = User.builder()
                .name(name)
                .email(email)
                .build();

        return userRepository.save(user);
    }

}
