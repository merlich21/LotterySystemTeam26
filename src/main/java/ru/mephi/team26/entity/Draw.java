package ru.mephi.team26.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "draws")
@Getter
@Setter
public class Draw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private DrawStatus status;

    private int numbersCount;

    private int maxNumber;

    private OffsetDateTime createdAt;
}