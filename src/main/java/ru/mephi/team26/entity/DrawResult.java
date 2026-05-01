package ru.mephi.team26.entity;

import io.hypersistence.utils.hibernate.type.array.IntArrayType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

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

    @Type(IntArrayType.class)
    @Column(columnDefinition = "integer[]")
    private List<Integer> winningNumbers;

    private OffsetDateTime generatedAt;
}