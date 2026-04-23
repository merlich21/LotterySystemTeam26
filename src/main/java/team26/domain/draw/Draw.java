package team26.domain.draw;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import team26.domain.user.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lottery_draws")
public class Draw {

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

    @Column(
            name = "draw_number",
            nullable = false,
            unique = true,
            updatable = false,
            insertable = false,
            columnDefinition = "INTEGER GENERATED ALWAYS AS IDENTITY"
    )
    private Integer drawNumber;

    @Column(
            name = "draw_name",
            length = 100
    )
    private String drawName;

    @Column(
            name = "total_tickets",
            nullable = false
    )
    private Integer totalTickets = 0;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private DrawStatus status = DrawStatus.SCHEDULED;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Draw() {}

    protected Draw(String drawName) {
        this.drawName = drawName;
    }

    public UUID getId() {
        return id;
    }

    public Integer getDrawNumber() {
        return drawNumber;
    }

    public String getDrawName() {
        return drawName;
    }

    public Integer getTotalTickets() {
        return totalTickets;
    }

    public DrawStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setDrawName(String drawName) {
        this.drawName = drawName;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }

    public void setStatus(DrawStatus status) {
        this.status = status;
    }
}
