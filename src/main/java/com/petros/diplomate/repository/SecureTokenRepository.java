package com.petros.diplomate.repository;

import com.petros.diplomate.model.SecureToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecureTokenRepository extends JpaRepository<SecureToken, Long> {

    Optional<SecureToken> findByUserId(Long userId);

    Optional<SecureToken> findByToken(String token);

}
