package team26.domain.lotteryTicket;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.user.User;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "lottery_tickets")
public class LotteryTicket {

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
        this.lotteryDraw = lotteryDraw;
        this.ticketNumbers = validateAndCopyNumbers(ticketNumbers);
    }

    public Integer[] getTicketNumbers() {
        return ticketNumbers.clone();
    }

    private Integer[] validateAndCopyNumbers(Integer[] ticketNumbers) {
        if (ticketNumbers == null) {
            throw new IllegalArgumentException("Массив чисел не может быть null");
        }

        if (ticketNumbers.length != 5) {
            throw new IllegalArgumentException("Должно быть ровно 5 чисел, получено: " + ticketNumbers.length);
        }

        for (Integer num : ticketNumbers) {
            if (num == null) {
                throw new IllegalArgumentException("Число не может быть null");
            }

            if (num < 1 || num > 45) {
                throw new IllegalArgumentException("Число " + num + " вне диапазона 1-45");
            }

        }
        long distinctCount = Arrays.stream(ticketNumbers).distinct().count();
        if (distinctCount != 5) {
            throw new IllegalArgumentException("Все числа должны быть уникальны");
        }

        return ticketNumbers.clone();
    }
}
