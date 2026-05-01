package team26.domain.lotteryDraw;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import team26.domain.lotteryDrawResult.LotteryDrawResult;
import team26.domain.lotteryTicket.LotteryTicket;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "lottery_draws")
public class LotteryDraw {

    @Getter
    @Setter
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

    @Getter
    @Setter
    @Column(
            name = "draw_number",
            nullable = false,
            unique = true,
            updatable = false,
            insertable = false,
            columnDefinition = "INTEGER GENERATED ALWAYS AS IDENTITY"
    )
    private Integer drawNumber;

    @Getter
    @Setter
    @Column(
            name = "draw_name",
            length = 100
    )
    private String drawName;

    @Getter
    @Setter
    @Column(
            name = "total_tickets",
            nullable = false
    )
    private Integer totalTickets = 0;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private LotteryDrawStatus status = LotteryDrawStatus.SCHEDULED;

    @Getter
    @Setter
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "lotteryDraw", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotteryTicket> lotteryTickets = new ArrayList<>();

    @OneToOne(mappedBy = "lotteryDraw", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private LotteryDrawResult lotteryDrawResult;

    protected LotteryDraw() {
    }

    public LotteryDraw(String drawName) {
        this.drawName = drawName;
    }

    public boolean hasResult() {
        return lotteryDrawResult != null;
    }

    public Optional<LotteryDrawResult> getResultOptional() {
        return Optional.ofNullable(lotteryDrawResult);
    }
}
