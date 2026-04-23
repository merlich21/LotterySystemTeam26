package team26.domain.user;

import jakarta.persistence.*;
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
            name = "name",
            length = 100,
            nullable = false
    )
    private String name;

    @Getter
    @Setter
    @Column(
            name = "surname",
            length = 100,
            nullable = false
    )
    private String surname;

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
    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;

    @Getter
    @Setter
    @Column(
            name = "phone",
            length = 12,
            unique = true
    )
    private String phone;

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

    public User(String name, String surname, String login,
                String email, String phone, String hashedPassword) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.email = email;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
    }
}
