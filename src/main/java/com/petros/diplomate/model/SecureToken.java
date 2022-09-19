package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class SecureToken {

    @Id
    @SequenceGenerator(name = "secure_token_sequence", sequenceName = "secure_token_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "secure_token_sequence")
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean redeemed;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    public SecureToken(String token, LocalDateTime expiresAt, boolean redeemed, User user) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.redeemed = redeemed;
        this.user = user;
    }

}
