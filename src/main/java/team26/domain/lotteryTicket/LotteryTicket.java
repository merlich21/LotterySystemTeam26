package team26.domain.lotteryTicket;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.user.User;
import team26.util.database.Helper;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "lottery_tickets")
public class LotteryTicket {

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_id")
    )
    private User user;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lottery_draw_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_lottery_draw_id")
    )
    private LotteryDraw lotteryDraw;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false,
            columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'"
    )
    private LotteryTicketStatus status = LotteryTicketStatus.PENDING;

    @Column(
            name = "ticket_numbers",
            nullable = false,
            columnDefinition = "INTEGER[5]"
    )
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Integer[] ticketNumbers;

    @Getter
    @Setter
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private OffsetDateTime createAt;

    protected LotteryTicket() {
    }

    public LotteryTicket(User user, LotteryDraw lotteryDraw, Integer[] ticketNumbers) {
        this.user = user;
        this.lotteryDraw = Objects.requireNonNull(lotteryDraw, "Розыгрыш обязателен");
        this.ticketNumbers = Helper.validateAndCopyNumbers(ticketNumbers);
    }

    public Integer[] getTicketNumbers() {
        return ticketNumbers.clone();
    }
}
