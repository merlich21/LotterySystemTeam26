package team26.domain.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import team26.domain.lotteryTicket.LotteryTicket;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Getter
    @Setter
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(
            name = "id",
            updatable = false,
            unique = true,
            nullable = false,
            columnDefinition = "UUID"
    )
    @EqualsAndHashCode.Include
    private UUID id;

    @Getter
    @Setter
    @Column(
            name = "login",
            length = 50,
            nullable = false,
            unique = true
    )
    private String login;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            length = 20,
            nullable = false,
            columnDefinition = "VARCHAR(20) DEFAULT 'USER'"
    )
    private UserRole role = UserRole.USER;

    @Getter
    @Setter
    @Column(
            name = "hashed_password",
            nullable = false
    )
    private String hashedPassword;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotteryTicket> lotteryTickets = new ArrayList<>();

    protected User() {
    }

    public User(String login,
                String hashedPassword) {
        this.login = login;
        this.hashedPassword = hashedPassword;
    }
}
