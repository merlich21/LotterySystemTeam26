package team26.repository.lotteryDrawResult;

import team26.domain.lotteryDrawResult.LotteryDrawResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotteryDrawResultRepository {

    // CREATE
    LotteryDrawResult save(LotteryDrawResult result);

    // READ
    Optional<LotteryDrawResult> findById(UUID id);
    Optional<LotteryDrawResult> findByLotteryDrawId(UUID lotteryDrawId);
    List<LotteryDrawResult> findAll();

    // UPDATE
    LotteryDrawResult update(LotteryDrawResult result);
    void updateResultNumbers(UUID id, Integer[] resultNumbers);

    // DELETE
    void delete(UUID id);
    void deleteByLotteryDrawId(UUID lotteryDrawId);

    // EXISTS
    boolean existsById(UUID id);
    boolean existsByLotteryDrawId(UUID lotteryDrawId);
}