package ru.mephi.team26.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "draw_results")
@Getter
@Setter
public class DrawResult {
    @Id
    @Column(name = "draw_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "draw_id")
    private Draw draw;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "integer[]")
    private List<Integer> winningNumbers;

    private OffsetDateTime generatedAt;
}