package team26.domain.lotteryDrawResult;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import team26.domain.lotteryDraw.LotteryDraw;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "lottery_draws_result")
public class LotteryDrawResult {

    @Setter
    @Getter
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(
            name = "id",
            updatable = false,
            nullable = false,
            columnDefinition = "UUID DEFAULT gen_random_uuid()"
    )
    @EqualsAndHashCode.Include
    private UUID id;

    @Setter
    @Getter
    @Column(
            name = "result_numbers",
            nullable = false,
            columnDefinition = "INTEGER[5] DEFAULT generate_lottery_numbers()"
    )
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Integer[] resultNumbers;

    @Setter
    @Getter
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "lottery_draw_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_lottery_draws_id")
    )
    private LotteryDraw lotteryDraw;

    public LotteryDrawResult() {
    }

    @Builder
    public LotteryDrawResult(LotteryDraw lotteryDraw) {
        this.lotteryDraw = Objects.requireNonNull(lotteryDraw, "Розыгрыш обязателен");
    }

    public boolean hasNumber(int number) {
        return Arrays.asList(resultNumbers).contains(number);
    }
}
