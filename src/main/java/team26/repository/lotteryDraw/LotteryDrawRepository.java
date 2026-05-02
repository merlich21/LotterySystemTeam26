package team26.repository.lotteryDraw;

import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotteryDrawRepository {
    // CREATE
    LotteryDraw save(LotteryDraw lotteryDraw);

    //READ
    Optional<LotteryDraw> findById(UUID id);
    Optional<LotteryDraw> findByDrawName(String drawName);
    Optional<LotteryDraw> findByDrawNumber(Integer drawNumber);
    List<LotteryDraw> findAllByStatus(LotteryDrawStatus status);
    List<LotteryDraw> findAll();


    // UPDATE
    LotteryDraw udpate(LotteryDraw lotteryDraw);
    void updateStatus(UUID id, LotteryDrawStatus status);
    void incrementTotalTickets(UUID id);
    void decrementTotalTickets(UUID id);


    // DELETE
    void delete(UUID id);

    // EXISTS
    boolean existsByDrawNumber(Integer drawNumber);
    boolean existsByDrawName(String drawName);
}
