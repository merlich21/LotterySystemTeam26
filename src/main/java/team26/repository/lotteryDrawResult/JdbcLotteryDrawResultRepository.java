//package team26.repository.lotteryDrawResult;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import team26.config.database.DatabaseConfig;
//import team26.domain.lotteryDrawResult.LotteryDrawResult;
//import team26.util.database.ConverterData;
//
//import javax.sql.DataSource;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public class JdbcLotteryDrawResultRepository implements LotteryDrawResultRepository {
//
//    private static final Logger log = LoggerFactory.getLogger(JdbcLotteryDrawResultRepository.class);
//    private final DataSource dataSource;
//
//    public JdbcLotteryDrawResultRepository() {
//        this.dataSource = DatabaseConfig.getDataSource();
//    }
//
//    @Override
//    public LotteryDrawResult save(LotteryDrawResult result) {
//        // Если числа не переданы - они сгенерируются через DEFAULT generate_lottery_numbers()
//        String sql;
//        boolean hasNumbers = result.getResultNumbers() != null && result.getResultNumbers().length > 0;
//
//        if (hasNumbers) {
//            sql = """
//                INSERT INTO lottery_draws_result (lottery_draw_id, result_numbers)
//                VALUES (?, ?)
//                RETURNING *;
//                """;
//        } else {
//            sql = """
//                INSERT INTO lottery_draws_result (lottery_draw_id)
//                VALUES (?)
//                RETURNING *;
//                """;
//        }
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, result.getLotteryDraw().getId());
//
//            if (hasNumbers) {
//                Array numbersArray = conn.createArrayOf("INTEGER", result.getResultNumbers());
//                stmt.setArray(2, numbersArray);
//            }
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    ConverterData.convertDataToLotteryDrawResult(rs);
//                    log.info("Lottery draw result saved for draw: {}",
//                            result.getLotteryDraw().getId());
//                }
//            }
//        } catch (SQLException e) {
//            log.error("Error saving lottery draw result for draw: {}",
//                    result.getLotteryDraw().getId(), e);
//            throw new RuntimeException("Failed to save lottery draw result", e);
//        }
//
//        return result;
//    }
//
//    @Override
//    public Optional<LotteryDrawResult> findById(UUID id) {
//        String sql = "SELECT * FROM lottery_draws_result WHERE id = ?";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, id);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return Optional.of(ConverterData.convertDataToLotteryDrawResult(rs));
//                }
//            }
//        } catch (SQLException e) {
//            log.error("Error finding lottery draw result by id: {}", id, e);
//            throw new RuntimeException("Failed to find lottery draw result", e);
//        }
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<LotteryDrawResult> findByLotteryDrawId(UUID lotteryDrawId) {
//        String sql = "SELECT * FROM lottery_draws_result WHERE lottery_draw_id = ?";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, lotteryDrawId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return Optional.of(ConverterData.convertDataToLotteryDrawResult(rs));
//                }
//            }
//        } catch (SQLException e) {
//            log.error("Error finding lottery draw result by draw id: {}", lotteryDrawId, e);
//            throw new RuntimeException("Failed to find lottery draw result", e);
//        }
//        return Optional.empty();
//    }
//
//    @Override
//    public List<LotteryDrawResult> findAll() {
//        String sql = "SELECT * FROM lottery_draws_result ORDER BY created_at DESC";
//        List<LotteryDrawResult> results = new ArrayList<>();
//
//        try (Connection conn = dataSource.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//
//            while (rs.next()) {
//                results.add(ConverterData.convertDataToLotteryDrawResult(rs));
//            }
//        } catch (SQLException e) {
//            log.error("Error finding all lottery draw results", e);
//            throw new RuntimeException("Failed to find lottery draw results", e);
//        }
//        return results;
//    }
//
//    @Override
//    public LotteryDrawResult update(LotteryDrawResult result) {
//        String sql = """
//            UPDATE lottery_draws_result
//            SET result_numbers = ?
//            WHERE id = ?
//            """;
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            Array numbersArray = conn.createArrayOf("INTEGER", result.getResultNumbers());
//            stmt.setArray(1, numbersArray);
//            stmt.setObject(2, result.getId());
//
//            int updated = stmt.executeUpdate();
//            if (updated == 0) {
//                throw new RuntimeException("Lottery draw result not found for update: " + result.getId());
//            }
//            log.info("Lottery draw result updated: {}", result.getId());
//        } catch (SQLException e) {
//            log.error("Error updating lottery draw result: {}", result.getId(), e);
//            throw new RuntimeException("Failed to update lottery draw result", e);
//        }
//        return result;
//    }
//
//    @Override
//    public void updateResultNumbers(UUID id, Integer[] resultNumbers) {
//        if (resultNumbers == null || resultNumbers.length != 5) {
//            throw new IllegalArgumentException("Result numbers must be an array of 5 integers");
//        }
//
//        String sql = "UPDATE lottery_draws_result SET result_numbers = ? WHERE id = ?";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            Array numbersArray = conn.createArrayOf("INTEGER", resultNumbers);
//            stmt.setArray(1, numbersArray);
//            stmt.setObject(2, id);
//
//            int updated = stmt.executeUpdate();
//            if (updated == 0) {
//                throw new RuntimeException("Lottery draw result not found for numbers update: " + id);
//            }
//            log.info("Lottery draw result numbers updated: {}", id);
//        } catch (SQLException e) {
//            log.error("Error updating lottery draw result numbers: {}", id, e);
//            throw new RuntimeException("Failed to update lottery draw result numbers", e);
//        }
//    }
//
//    @Override
//    public void delete(UUID id) {
//        String sql = "DELETE FROM lottery_draws_result WHERE id = ?";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, id);
//
//            int deleted = stmt.executeUpdate();
//            if (deleted == 0) {
//                throw new RuntimeException("Lottery draw result not found for deletion: " + id);
//            }
//            log.info("Lottery draw result deleted: {}", id);
//        } catch (SQLException e) {
//            log.error("Error deleting lottery draw result: {}", id, e);
//            throw new RuntimeException("Failed to delete lottery draw result", e);
//        }
//    }
//
//    @Override
//    public void deleteByLotteryDrawId(UUID lotteryDrawId) {
//        String sql = "DELETE FROM lottery_draws_result WHERE lottery_draw_id = ?";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, lotteryDrawId);
//
//            int deleted = stmt.executeUpdate();
//            if (deleted == 0) {
//                log.warn("No lottery draw result found for deletion by draw id: {}", lotteryDrawId);
//            } else {
//                log.info("Lottery draw result deleted for draw: {}", lotteryDrawId);
//            }
//        } catch (SQLException e) {
//            log.error("Error deleting lottery draw result by draw id: {}", lotteryDrawId, e);
//            throw new RuntimeException("Failed to delete lottery draw result", e);
//        }
//    }
//
//    @Override
//    public boolean existsById(UUID id) {
//        String sql = "SELECT EXISTS(SELECT 1 FROM lottery_draws_result WHERE id = ?)";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, id);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getBoolean(1);
//                }
//            }
//        } catch (SQLException e) {
//            log.error("Error checking lottery draw result existence by id: {}", id, e);
//            throw new RuntimeException("Failed to check lottery draw result existence", e);
//        }
//        return false;
//    }
//
//    @Override
//    public boolean existsByLotteryDrawId(UUID lotteryDrawId) {
//        String sql = "SELECT EXISTS(SELECT 1 FROM lottery_draws_result WHERE lottery_draw_id = ?)";
//
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setObject(1, lotteryDrawId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getBoolean(1);
//                }
//            }
//        } catch (SQLException e) {
//            log.error("Error checking lottery draw result existence by draw id: {}", lotteryDrawId, e);
//            throw new RuntimeException("Failed to check lottery draw result existence", e);
//        }
//        return false;
//    }
//}