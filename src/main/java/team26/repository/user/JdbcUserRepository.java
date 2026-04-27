package team26.repository.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.config.database.DatabaseConfig;
import team26.domain.user.User;
import team26.domain.user.UserRole;
import team26.util.database.ConverterData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserRepository implements UserRepository {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(JdbcUserRepository.class);

    public JdbcUserRepository() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    @Override
    public User save(User user) {

        String sql = """
                INSERT INTO users (name, surname, login, email, phone, role, hashed_password)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id, created_at;
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getSurname());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getHashedPassword());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getObject("id", UUID.class));
                    user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    logger.info("User saved with id: {}", user.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving user: {}", user.getLogin(), e);
            throw new RuntimeException("Failed to save user", e);
        }
        return user;
    }

    @Override
    public Optional<User> findByLogin(String login) {
        String sql = """
                  SELECT * FROM users WHERE login = ?
                """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ConverterData.convertDataToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public List<User> findAllByRole(UserRole role) {
        return List.of();
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(User user) {

    }

    @Override
    public boolean existsByLogin(String login) {
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }
}
