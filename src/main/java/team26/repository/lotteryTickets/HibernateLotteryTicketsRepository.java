package team26.repository.lotteryTickets;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.lotteryTicket.LotteryTicketStatus;
import team26.util.database.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateLotteryTicketsRepository implements LotteryTicketsRepository {

    private static final Logger log = LoggerFactory.getLogger(HibernateLotteryTicketsRepository.class);
    private final SessionFactory sessionFactory;

    public HibernateLotteryTicketsRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // CREATE
    @Override
    public LotteryTicket save(LotteryTicket ticket) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            session.persist(ticket);

            tx.commit();
            log.info("Lottery ticket saved: {}", ticket.getId());

            return ticket;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to save ticket", e);
        }
    }

    // READ
    @Override
    public Optional<LotteryTicket> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(LotteryTicket.class, id));
        }
    }

    @Override
    public List<LotteryTicket> findByUserId(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryTicket lt
                    WHERE lt.user.id = :userId
                    ORDER BY lt.createAt DESC
                    """, LotteryTicket.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    @Override
    public List<LotteryTicket> findByLotteryDrawId(UUID drawId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryTicket lt
                    WHERE lt.lotteryDraw.id = :drawId
                    ORDER BY lt.createAt DESC
                    """, LotteryTicket.class)
                    .setParameter("drawId", drawId)
                    .list();
        }
    }

    @Override
    public List<LotteryTicket> findByUserIdAndDrawId(UUID userId, UUID drawId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryTicket lt
                    WHERE lt.user.id = :userId
                      AND lt.lotteryDraw.id = :drawId
                    ORDER BY lt.createAt DESC
                    """, LotteryTicket.class)
                    .setParameter("userId", userId)
                    .setParameter("drawId", drawId)
                    .list();
        }
    }

    @Override
    public List<LotteryTicket> findByStatus(LotteryTicketStatus status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryTicket lt
                    WHERE lt.status = :status
                    ORDER BY lt.createAt DESC
                    """, LotteryTicket.class)
                    .setParameter("status", status)
                    .list();
        }
    }

    @Override
    public List<LotteryTicket> findByDrawIdAndStatus(UUID drawId, LotteryTicketStatus status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryTicket lt
                    WHERE lt.lotteryDraw.id = :drawId
                      AND lt.status = :status
                    ORDER BY lt.createAt DESC
                    """, LotteryTicket.class)
                    .setParameter("drawId", drawId)
                    .setParameter("status", status)
                    .list();
        }
    }

    @Override
    public List<LotteryTicket> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryTicket lt
                    ORDER BY lt.createAt DESC
                    """, LotteryTicket.class).list();
        }
    }

    // COUNT
    @Override
    public int countByDrawId(UUID drawId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("""
                    SELECT COUNT(lt)
                    FROM LotteryTicket lt
                    WHERE lt.lotteryDraw.id = :drawId
                    """, Long.class)
                    .setParameter("drawId", drawId)
                    .uniqueResult();

            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public int countByDrawIdAndStatus(UUID drawId, LotteryTicketStatus status) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("""
                    SELECT COUNT(lt)
                    FROM LotteryTicket lt
                    WHERE lt.lotteryDraw.id = :drawId
                      AND lt.status = :status
                    """, Long.class)
                    .setParameter("drawId", drawId)
                    .setParameter("status", status)
                    .uniqueResult();

            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public int countByUserId(UUID userId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("""
                    SELECT COUNT(lt)
                    FROM LotteryTicket lt
                    WHERE lt.user.id = :userId
                    """, Long.class)
                    .setParameter("userId", userId)
                    .uniqueResult();

            return count != null ? count.intValue() : 0;
        }
    }

    // UPDATE
    @Override
    public LotteryTicket update(LotteryTicket ticket) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            LotteryTicket merged = (LotteryTicket) session.merge(ticket);

            tx.commit();
            log.info("Lottery ticket updated: {}", ticket.getId());

            return merged;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to update ticket", e);
        }
    }

    @Override
    public void updateStatus(UUID id, LotteryTicketStatus status) {
        executeUpdate("""
                UPDATE LotteryTicket lt
                SET lt.status = :status
                WHERE lt.id = :id
                """, id, status, null);
    }

    @Override
    public void updateStatusForDraw(UUID drawId, LotteryTicketStatus oldStatus, LotteryTicketStatus newStatus) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            int updated = session.createQuery("""
                    UPDATE LotteryTicket lt
                    SET lt.status = :newStatus
                    WHERE lt.lotteryDraw.id = :drawId
                      AND lt.status = :oldStatus
                    """)
                    .setParameter("newStatus", newStatus)
                    .setParameter("drawId", drawId)
                    .setParameter("oldStatus", oldStatus)
                    .executeUpdate();

            tx.commit();
            log.info("Updated {} tickets for draw {}", updated, drawId);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to update statuses", e);
        }
    }

    private void executeUpdate(String hql, UUID id, LotteryTicketStatus status, Integer unused) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            int updated = session.createQuery(hql)
                    .setParameter("status", status)
                    .setParameter("id", id)
                    .executeUpdate();

            if (updated == 0) {
                throw new RuntimeException("Ticket not found: " + id);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    // DELETE
    @Override
    public void delete(UUID id) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            LotteryTicket ticket = session.get(LotteryTicket.class, id);
            if (ticket == null) {
                throw new RuntimeException("Ticket not found: " + id);
            }

            session.remove(ticket);

            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByDrawId(UUID drawId) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            int deleted = session.createQuery("""
                    DELETE FROM LotteryTicket lt
                    WHERE lt.lotteryDraw.id = :drawId
                    """)
                    .setParameter("drawId", drawId)
                    .executeUpdate();

            tx.commit();
            log.info("Deleted {} tickets for draw {}", deleted, drawId);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    // EXISTS
    @Override
    public boolean existsById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("""
                    SELECT COUNT(lt)
                    FROM LotteryTicket lt
                    WHERE lt.id = :id
                    """, Long.class)
                    .setParameter("id", id)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }

    @Override
    public boolean existsByUserAndDraw(UUID userId, UUID drawId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("""
                    SELECT COUNT(lt)
                    FROM LotteryTicket lt
                    WHERE lt.user.id = :userId
                      AND lt.lotteryDraw.id = :drawId
                    """, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("drawId", drawId)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }
}