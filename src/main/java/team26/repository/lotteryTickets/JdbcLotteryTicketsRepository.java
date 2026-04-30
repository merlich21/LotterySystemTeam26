package team26.repository.lotteryTickets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.config.database.DatabaseConfig;
import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.lotteryTicket.LotteryTicketStatus;
import team26.util.database.ConverterData;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcLotteryTicketsRepository implements LotteryTicketsRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcLotteryTicketsRepository.class);
    private final DataSource dataSource;

    public JdbcLotteryTicketsRepository() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    @Override
    public LotteryTicket save(LotteryTicket lotteryTicket) {
        String sql = """
                INSERT INTO lottery_tickets (user_id, lottery_draw_id, ticket_numbers)
                VALUES (?, ?, ?)
                RETURNING *;
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, lotteryTicket.getUser().getId());
            stmt.setObject(2, lotteryTicket.getLotteryDraw().getId());

            Array ticketNumbersArray = conn.createArrayOf("INTEGER", lotteryTicket.getTicketNumbers());
            stmt.setArray(3, ticketNumbersArray);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    lotteryTicket = ConverterData.convertDataToLotteryTicket(rs);
                    log.info("Lottery ticket saved: {} (user: {}, draw: {})",
                            lotteryTicket.getId(),
                            lotteryTicket.getUser().getId(),
                            lotteryTicket.getLotteryDraw().getId());
                }

            }
        } catch (SQLException e) {
            log.error("Error saving lottery ticket for user: {}",
                    lotteryTicket.getUser().getId(), e);
            throw new RuntimeException("Failed to save lottery ticket", e);
        }

        return lotteryTicket;
    }


    @Override
    public Optional<LotteryTicket> findById(UUID id) {
        String sql = "SELECT * FROM lottery_tickets WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ConverterData.convertDataToLotteryTicket(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding lottery ticket by id: {}", id, e);
            throw new RuntimeException("Failed to find lottery ticket", e);
        }
        return Optional.empty();
    }

    @Override
    public List<LotteryTicket> findByUserId(UUID userId) {
        String sql = "SELECT * FROM lottery_tickets WHERE user_id = ? ORDER BY created_at DESC";
        List<LotteryTicket> tickets = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(ConverterData.convertDataToLotteryTicket(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding lottery tickets by user: {}", userId, e);
            throw new RuntimeException("Failed to find lottery tickets", e);
        }
        return tickets;
    }

    @Override
    public List<LotteryTicket> findByLotteryDrawId(UUID lotteryDrawId) {
        String sql = "SELECT * FROM lottery_tickets WHERE lottery_draw_id = ? ORDER BY created_at DESC";
        List<LotteryTicket> tickets = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, lotteryDrawId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(ConverterData.convertDataToLotteryTicket(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding lottery tickets by draw: {}", lotteryDrawId, e);
            throw new RuntimeException("Failed to find lottery tickets", e);
        }
        return tickets;
    }

    @Override
    public List<LotteryTicket> findByUserIdAndDrawId(UUID userId, UUID lotteryDrawId) {
        String sql = """
                SELECT * FROM lottery_tickets 
                WHERE user_id = ? AND lottery_draw_id = ? 
                ORDER BY created_at DESC
                """;
        List<LotteryTicket> tickets = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, lotteryDrawId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(ConverterData.convertDataToLotteryTicket(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding lottery tickets by user {} and draw {}", userId, lotteryDrawId, e);
            throw new RuntimeException("Failed to find lottery tickets", e);
        }
        return tickets;
    }

    @Override
    public List<LotteryTicket> findByStatus(LotteryTicketStatus status) {
        String sql = "SELECT * FROM lottery_tickets WHERE status = ? ORDER BY created_at DESC";
        List<LotteryTicket> tickets = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(ConverterData.convertDataToLotteryTicket(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding lottery tickets by status: {}", status, e);
            throw new RuntimeException("Failed to find lottery tickets", e);
        }
        return tickets;
    }

    @Override
    public List<LotteryTicket> findByDrawIdAndStatus(UUID lotteryDrawId, LotteryTicketStatus status) {
        String sql = """
                SELECT * FROM lottery_tickets 
                WHERE lottery_draw_id = ? AND status = ? 
                ORDER BY created_at DESC
                """;
        List<LotteryTicket> tickets = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, lotteryDrawId);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(ConverterData.convertDataToLotteryTicket(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding lottery tickets by draw {} and status {}", lotteryDrawId, status, e);
            throw new RuntimeException("Failed to find lottery tickets", e);
        }
        return tickets;
    }

    @Override
    public List<LotteryTicket> findAll() {
        String sql = "SELECT * FROM lottery_tickets ORDER BY created_at DESC";
        List<LotteryTicket> tickets = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tickets.add(ConverterData.convertDataToLotteryTicket(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all lottery tickets", e);
            throw new RuntimeException("Failed to find lottery tickets", e);
        }
        return tickets;
    }

    @Override
    public int countByDrawId(UUID lotteryDrawId) {
        String sql = "SELECT COUNT(*) FROM lottery_tickets WHERE lottery_draw_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, lotteryDrawId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting tickets by draw: {}", lotteryDrawId, e);
            throw new RuntimeException("Failed to count tickets", e);
        }
        return 0;
    }

    @Override
    public int countByDrawIdAndStatus(UUID lotteryDrawId, LotteryTicketStatus status) {
        String sql = "SELECT COUNT(*) FROM lottery_tickets WHERE lottery_draw_id = ? AND status = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, lotteryDrawId);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting tickets by draw {} and status {}", lotteryDrawId, status, e);
            throw new RuntimeException("Failed to count tickets", e);
        }
        return 0;
    }

    @Override
    public int countByUserId(UUID userId) {
        String sql = "SELECT COUNT(*) FROM lottery_tickets WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting tickets by user: {}", userId, e);
            throw new RuntimeException("Failed to count tickets", e);
        }
        return 0;
    }

    @Override
    public LotteryTicket update(LotteryTicket lotteryTicket) {
        String sql = """
                UPDATE lottery_tickets 
                SET status = ?, ticket_numbers = ?
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lotteryTicket.getStatus().name());

            Array ticketNumbersArray = conn.createArrayOf("INTEGER", lotteryTicket.getTicketNumbers());
            stmt.setArray(2, ticketNumbersArray);

            stmt.setObject(3, lotteryTicket.getId());

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Lottery ticket not found for update: " + lotteryTicket.getId());
            }
            log.info("Lottery ticket updated: {}", lotteryTicket.getId());
        } catch (SQLException e) {
            log.error("Error updating lottery ticket: {}", lotteryTicket.getId(), e);
            throw new RuntimeException("Failed to update lottery ticket", e);
        }
        return lotteryTicket;
    }

    @Override
    public void updateStatus(UUID id, LotteryTicketStatus status) {
        String sql = "UPDATE lottery_tickets SET status = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setObject(2, id);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Lottery ticket not found for status update: " + id);
            }
            log.info("Lottery ticket status updated: {} -> {}", id, status);
        } catch (SQLException e) {
            log.error("Error updating lottery ticket status: {}", id, e);
            throw new RuntimeException("Failed to update lottery ticket status", e);
        }
    }

    @Override
    public void updateStatusForDraw(UUID lotteryDrawId, LotteryTicketStatus oldStatus, LotteryTicketStatus newStatus) {
        String sql = """
                UPDATE lottery_tickets 
                SET status = ? 
                WHERE lottery_draw_id = ? AND status = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.name());
            stmt.setObject(2, lotteryDrawId);
            stmt.setString(3, oldStatus.name());

            int updated = stmt.executeUpdate();
            log.info("Lottery tickets status updated for draw {}: {} -> {} ({} tickets affected)",
                    lotteryDrawId, oldStatus, newStatus, updated);
        } catch (SQLException e) {
            log.error("Error updating lottery tickets status for draw: {}", lotteryDrawId, e);
            throw new RuntimeException("Failed to update lottery tickets status", e);
        }
    }


    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM lottery_tickets WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new RuntimeException("Lottery ticket not found for deletion: " + id);
            }
            log.info("Lottery ticket deleted: {}", id);
        } catch (SQLException e) {
            log.error("Error deleting lottery ticket: {}", id, e);
            throw new RuntimeException("Failed to delete lottery ticket", e);
        }
    }

    @Override
    public void deleteByDrawId(UUID lotteryDrawId) {
        String sql = "DELETE FROM lottery_tickets WHERE lottery_draw_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, lotteryDrawId);

            int deleted = stmt.executeUpdate();
            log.info("Lottery tickets deleted for draw {}: {} tickets", lotteryDrawId, deleted);
        } catch (SQLException e) {
            log.error("Error deleting lottery tickets by draw: {}", lotteryDrawId, e);
            throw new RuntimeException("Failed to delete lottery tickets", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM lottery_tickets WHERE id = ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error checking lottery ticket existence by id: {}", id, e);
            throw new RuntimeException("Failed to check lottery ticket existence", e);
        }
        return false;
    }

    @Override
    public boolean existsByUserAndDraw(UUID userId, UUID lotteryDrawId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM lottery_tickets WHERE user_id = ? AND lottery_draw_id = ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, lotteryDrawId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error checking lottery ticket existence for user {} and draw {}", userId, lotteryDrawId, e);
            throw new RuntimeException("Failed to check lottery ticket existence", e);
        }
        return false;
    }
}