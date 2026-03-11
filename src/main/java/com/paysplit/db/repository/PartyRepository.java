package com.paysplit.db.repository;

import com.paysplit.db.domain.Party;
import jakarta.servlet.http.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyRepository extends JpaRepository<Party, Long> {
    boolean existsByInviteCode(String code);
    Optional<Party> findByLeaderId(Long leaderId);
}
