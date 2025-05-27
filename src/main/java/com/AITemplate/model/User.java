package com.AITemplate.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 고유 ID

    @Column(nullable = false, unique = true)
    private String userId;  // 사용자 ID (로그인 시 사용)

    @Column(nullable = false)
    private String username;  // 사용자 이름

    @Column(nullable = false, unique = true)
    private String email;  // 이메일

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String provider; // "local" or "google"


}
