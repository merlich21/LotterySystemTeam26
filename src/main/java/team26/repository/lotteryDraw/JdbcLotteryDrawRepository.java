package team26.repository.lotteryDraw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.config.database.DatabaseConfig;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;
import team26.util.database.ConverterData;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcLotteryDrawRepository implements LotteryDrawRepository {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(JdbcLotteryDrawRepository.class);

    public JdbcLotteryDrawRepository() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    @Override
    public LotteryDraw save(LotteryDraw lotteryDraw) {
        String sql = """
                   INSERT INTO lottery_draws (draw_name)
                   VALUES (?)
                   RETURNING *;
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lotteryDraw.getDrawName());
            try (ResultSet rs = stmt.executeQuery();) {
                if (rs.next()) {
                    lotteryDraw.setId(rs.getObject("id", UUID.class));
                    lotteryDraw.setDrawNumber(rs.getObject("draw_number", Integer.class));
                    lotteryDraw.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    logger.info("Lottery draw saved by dataBase " + lotteryDraw.getDrawName(), lotteryDraw.getDrawName());
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving user: {}", lotteryDraw.getDrawName(), e);
            throw new RuntimeException("Failed to save user", e);
        }

        return lotteryDraw;
    }

    @Override
    public Optional<LotteryDraw> findById(UUID id) {
        String sql = "SELECT * FROM lottery_draws WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ConverterData.convertDataToLotteryDraw(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding lottery draw by id: {}", id, e);
            throw new RuntimeException("Failed to find lottery draw", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LotteryDraw> findByDrawName(String drawName) {
        String sql = "SELECT * FROM lottery_draws WHERE draw_name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, drawName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ConverterData.convertDataToLotteryDraw(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding lottery draw by name: {}", drawName, e);
            throw new RuntimeException("Failed to find lottery draw", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LotteryDraw> findByDrawNumber(Integer drawNumber) {
        String sql = "SELECT * FROM lottery_draws WHERE draw_number = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, drawNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ConverterData.convertDataToLotteryDraw(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding lottery draw by number: {}", drawNumber, e);
            throw new RuntimeException("Failed to find lottery draw", e);
        }
        return Optional.empty();
    }

    @Override
    public List<LotteryDraw> findAllByStatus(LotteryDrawStatus status) {
        String sql = "SELECT * FROM lottery_draws WHERE status = ? ORDER BY created_at DESC";
        List<LotteryDraw> draws = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    draws.add(ConverterData.convertDataToLotteryDraw(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding lottery draws by status: {}", status, e);
            throw new RuntimeException("Failed to find lottery draws", e);
        }
        return draws;
    }

    @Override
    public List<LotteryDraw> findAll() {
        String sql = "SELECT * FROM lottery_draws ORDER BY draw_number DESC";
        List<LotteryDraw> draws = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                draws.add(ConverterData.convertDataToLotteryDraw(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all lottery draws", e);
            throw new RuntimeException("Failed to find lottery draws", e);
        }
        return draws;
    }

    @Override
    public void incrementTotalTickets(UUID id) {
        String sql = """
                UPDATE lottery_draws 
                SET total_tickets = COALESCE(total_tickets, 0) + 1 
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Lottery draw not found: " + id);
            }
            logger.info("Lottery draw total_tickets incremented: {}", id);
        } catch (SQLException e) {
            logger.error("Error incrementing lottery draw tickets: {}", id, e);
            throw new RuntimeException("Failed to increment tickets", e);
        }
    }

    @Override
    public void decrementTotalTickets(UUID id) {
        String sql = """
                UPDATE lottery_draws 
                SET total_tickets = COALESCE(total_tickets, 0) - 1 
                WHERE id = ? AND total_tickets > 0
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Cannot decrement tickets for draw: " + id + " (not found or already 0)");
            }
            logger.info("Lottery draw total_tickets decremented: {}", id);
        } catch (SQLException e) {
            logger.error("Error decrementing lottery draw tickets: {}", id, e);
            throw new RuntimeException("Failed to decrement tickets", e);
        }
    }

    @Override
    public LotteryDraw udpate(LotteryDraw lotteryDraw) {
        String sql = """
                UPDATE lottery_draws 
                SET draw_name = ?, status = ?, total_tickets = ?
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lotteryDraw.getDrawName());
            stmt.setString(2, lotteryDraw.getStatus().name());
            stmt.setInt(3, lotteryDraw.getTotalTickets() != null ? lotteryDraw.getTotalTickets() : 0);
            stmt.setObject(4, lotteryDraw.getId());

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Lottery draw not found for update: " + lotteryDraw.getId());
            }
            logger.info("Lottery draw updated: '{}' (id: {})", lotteryDraw.getDrawName(), lotteryDraw.getId());
        } catch (SQLException e) {
            logger.error("Error updating lottery draw: {}", lotteryDraw.getId(), e);
            throw new RuntimeException("Failed to update lottery draw", e);
        }
        return lotteryDraw;
    }

    @Override
    public void updateStatus(UUID id, LotteryDrawStatus status) {
        String sql = "UPDATE lottery_draws SET status = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setObject(2, id);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Lottery draw not found for status update: " + id);
            }
            logger.info("Lottery draw status updated: {} -> {}", id, status);
        } catch (SQLException e) {
            logger.error("Error updating lottery draw status: {}", id, e);
            throw new RuntimeException("Failed to update lottery draw status", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM lottery_draws WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new RuntimeException("Lottery draw not found for deletion: " + id);
            }
            logger.info("Lottery draw deleted: {}", id);
        } catch (SQLException e) {
            logger.error("Error deleting lottery draw: {}", id, e);
            throw new RuntimeException("Failed to delete lottery draw", e);
        }
    }

    @Override
    public boolean existsByDrawNumber(Integer drawNumber) {
        String sql = "SELECT EXISTS(SELECT 1 FROM lottery_draws WHERE draw_number = ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, drawNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking lottery draw existence by number: {}", drawNumber, e);
            throw new RuntimeException("Failed to check lottery draw existence", e);
        }
        return false;
    }

    @Override
    public boolean existsByDrawName(String drawName) {
        String sql = "SELECT EXISTS(SELECT 1 FROM lottery_draws WHERE draw_name = ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, drawName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking lottery draw existence by number: {}", drawName, e);
            throw new RuntimeException("Failed to check lottery draw existence", e);
        }
        return false;
    }
}
