package com.example.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 — FK อยู่ฝั่งนี้ (owning side), unique = true บังคับว่า user หนึ่งคนมีได้แค่ 1 profile
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "risk_tolerance", length = 20)
    private String riskTolerance; // LOW / MEDIUM / HIGH

    @Column(name = "investment_goal", length = 255)
    private String investmentGoal;

    @Column(length = 20)
    private String phone;
}
