package team26.repository.user;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.config.database.DatabaseConfig;
import team26.domain.user.User;
import team26.domain.user.UserRole;
import team26.repository.user.enums.UserExistsField;
import team26.repository.user.enums.UserSearchField;
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
                INSERT INTO users (login, role, hashed_password)
                VALUES (?, ?, ?)
                RETURNING id, created_at;
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getHashedPassword());

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
        if (login == null) return Optional.empty();
        return findByField(login, UserSearchField.LOGIN);
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM users WHERE id"  + " = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("User found: {}", id);
                    return Optional.of(ConverterData.convertDataToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting user: {}", id, e);
            throw new RuntimeException("Failed to find user", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAllByRole(UserRole role) {
        String sql = "SELECT * FROM users where role = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return ConverterData.convertDataToAllUsers(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting users by role: {}", e);
            throw new RuntimeException("Failed to find users by role", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            try (ResultSet rs = stmt.executeQuery()) {
                return ConverterData.convertDataToAllUsers(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting all users: {}", e);
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET login = ?, role = ?, hashed_password = ?
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getHashedPassword());
            stmt.setObject(4, user.getId());

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("User not found for update: " + user.getId());
            }

            logger.info("User updated: {}", user.getId());
        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getId(), e);
            throw new RuntimeException("Failed to update user", e);
        }

        return user;
    }

    @Override
    public void delete(UUID userId) {
        String sql = """
                 DELETE FROM users WHERE id = ?;
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setObject(1, userId);

            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new RuntimeException("User not found for delete: " + userId);
            }
            logger.info("User deleted: {}", userId);
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", userId, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean existsByLogin(String login) {
        if (login == null) return false;
        return existsByField(login, UserExistsField.LOGIN);
    }

    private Optional<User> findByField(String value, UserSearchField field) {
        String sql = "SELECT * FROM users WHERE " + field.getColumnName() + " = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setString(1, value);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("User found: {}", value);
                    return Optional.of(ConverterData.convertDataToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting user: {}", value, e);
            throw new RuntimeException("Failed to find user", e);
        }
        return Optional.empty();
    }

    private boolean existsByField(String value, UserExistsField field) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE " + field.getColumnName() + " = ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean result = rs.getBoolean(1);
                    logger.info("User is exists by " + field + " " + value + ": " + result, result);
                    return result;
                }
            }
        } catch (SQLException e) {
            logger.error("Error exists user: {}", value, e);
            throw new RuntimeException("Failed to find user", e);
        }

        return false;
    }
}
