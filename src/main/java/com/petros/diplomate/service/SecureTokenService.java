package com.petros.diplomate.service;

import com.petros.diplomate.model.SecureToken;
import com.petros.diplomate.model.User;
import com.petros.diplomate.repository.SecureTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SecureTokenService {

    @Autowired
    private SecureTokenRepository secureTokenRepository;


    public SecureToken getSecureTokenByUserId(Long userId) throws IllegalStateException {
        return secureTokenRepository.findByUserId(userId).orElseThrow(
                () -> new IllegalStateException("Secure token with user ID " + userId + " not found")
        );
    }

    public SecureToken getSecureTokenByToken(String token) throws IllegalStateException {
        return secureTokenRepository.findByToken(token).orElseThrow(
                () -> new IllegalStateException("Secure token with token " + token + " not found")
        );
    }

    public void deleteSecureTokenById(Long secureTokenId) {
        secureTokenRepository.deleteById(secureTokenId);
    }

    public String initializeSecureToken(User user) {
        String token = UUID.randomUUID().toString();
        SecureToken secureToken = new SecureToken(token,LocalDateTime.now().plusMinutes(20),false,user);
        secureTokenRepository.save(secureToken);

        return token;
    }

    public String updateSecureToken(User user) {
        String token = UUID.randomUUID().toString();

        SecureToken secureToken = this.getSecureTokenByUserId(user.getId());
        secureToken.setToken(token);
        secureToken.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        secureToken.setRedeemed(false);

        secureTokenRepository.save(secureToken);

        return token;
    }

    public void emptyToken(SecureToken secureToken) {
        secureToken.setToken("");
        secureToken.setExpiresAt(LocalDateTime.now());
        secureTokenRepository.save(secureToken);
    }

}
