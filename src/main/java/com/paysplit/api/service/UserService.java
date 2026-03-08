package com.paysplit.api.service;

import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.User;
import com.paysplit.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.paysplit.common.error.user.UserErrorCode.LEFT_USER;
import static com.paysplit.common.error.user.UserErrorCode.USER_NOT_FOUND;

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
}
