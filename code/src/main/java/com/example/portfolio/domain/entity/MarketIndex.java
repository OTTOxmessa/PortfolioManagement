package com.example.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "market_indices", indexes = {
        @Index(name = "idx_market_indices_code", columnList = "index_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_code", nullable = false, unique = true, length = 20)
    private String indexCode; // เช่น SET, SPX

    @Column(nullable = false, length = 100)
    private String name;
}
