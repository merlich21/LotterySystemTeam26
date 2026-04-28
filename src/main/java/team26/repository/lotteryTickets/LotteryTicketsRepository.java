package team26.repository.lotteryTickets;

import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.lotteryTicket.LotteryTicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotteryTicketsRepository {

    // CREATE
    LotteryTicket save(LotteryTicket lotteryTicket);

    // READ
    Optional<LotteryTicket> findById(UUID id);
    List<LotteryTicket> findByUserId(UUID userId);
    List<LotteryTicket> findByLotteryDrawId(UUID lotteryDrawId);
    List<LotteryTicket> findByUserIdAndDrawId(UUID userId, UUID lotteryDrawId);
    List<LotteryTicket> findByStatus(LotteryTicketStatus status);
    List<LotteryTicket> findByDrawIdAndStatus(UUID lotteryDrawId, LotteryTicketStatus status);
    List<LotteryTicket> findAll();

    // COUNT
    int countByDrawId(UUID lotteryDrawId);
    int countByDrawIdAndStatus(UUID lotteryDrawId, LotteryTicketStatus status);
    int countByUserId(UUID userId);

    // UPDATE
    LotteryTicket update(LotteryTicket lotteryTicket);
    void updateStatus(UUID id, LotteryTicketStatus status);
    void updateStatusForDraw(UUID lotteryDrawId, LotteryTicketStatus oldStatus, LotteryTicketStatus newStatus);

    // DELETE
    void delete(UUID id);
    void deleteByDrawId(UUID lotteryDrawId);

    // EXISTS
    boolean existsById(UUID id);
    boolean existsByUserAndDraw(UUID userId, UUID lotteryDrawId);
}
