package team26.repository.lotteryDraw;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;
import team26.util.database.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateLotteryDrawRepository implements LotteryDrawRepository {

    private static final Logger logger = LoggerFactory.getLogger(HibernateLotteryDrawRepository.class);
    private final SessionFactory sessionFactory;

    public HibernateLotteryDrawRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // CREATE
    @Override
    public LotteryDraw save(LotteryDraw lotteryDraw) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            session.persist(lotteryDraw);

            tx.commit();
            logger.info("Lottery draw saved: {}", lotteryDraw.getId());

            return lotteryDraw;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error saving lottery draw: {}", lotteryDraw.getDrawName(), e);
            throw new RuntimeException("Failed to save lottery draw", e);
        }
    }

    // READ
    @Override
    public Optional<LotteryDraw> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(LotteryDraw.class, id));
        }
    }

    @Override
    public Optional<LotteryDraw> findByDrawName(String drawName) {
        try (Session session = sessionFactory.openSession()) {
            Query<LotteryDraw> query = session.createQuery(
                    "FROM LotteryDraw ld WHERE ld.drawName = :name", LotteryDraw.class);
            query.setParameter("name", drawName);

            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<LotteryDraw> findByDrawNumber(Integer drawNumber) {
        try (Session session = sessionFactory.openSession()) {
            Query<LotteryDraw> query = session.createQuery(
                    "FROM LotteryDraw ld WHERE ld.drawNumber = :num", LotteryDraw.class);
            query.setParameter("num", drawNumber);

            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<LotteryDraw> findAllByStatus(LotteryDrawStatus status) {
        try (Session session = sessionFactory.openSession()) {
            Query<LotteryDraw> query = session.createQuery(
                    "FROM LotteryDraw ld WHERE ld.status = :status ORDER BY ld.createdAt DESC",
                    LotteryDraw.class);
            query.setParameter("status", status);

            return query.list();
        }
    }

    @Override
    public List<LotteryDraw> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "FROM LotteryDraw ld ORDER BY ld.drawNumber DESC",
                    LotteryDraw.class
            ).list();
        }
    }

    // UPDATE
    @Override
    public LotteryDraw udpate(LotteryDraw lotteryDraw) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            LotteryDraw merged = (LotteryDraw) session.merge(lotteryDraw);

            tx.commit();
            logger.info("Lottery draw updated: {}", lotteryDraw.getId());

            return merged;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating lottery draw: {}", lotteryDraw.getId(), e);
            throw new RuntimeException("Failed to update lottery draw", e);
        }
    }

    @Override
    public void updateStatus(UUID id, LotteryDrawStatus status) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Query<?> query = session.createQuery(
                    "UPDATE LotteryDraw ld SET ld.status = :status WHERE ld.id = :id");
            query.setParameter("status", status);
            query.setParameter("id", id);

            int updated = query.executeUpdate();

            if (updated == 0) {
                throw new RuntimeException("Lottery draw not found: " + id);
            }

            tx.commit();
            logger.info("Lottery draw status updated: {} -> {}", id, status);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating status: {}", id, e);
            throw new RuntimeException("Failed to update status", e);
        }
    }

    @Override
    public void incrementTotalTickets(UUID id) {
        executeTicketUpdate(id, +1);
    }

    @Override
    public void decrementTotalTickets(UUID id) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Query<?> query = session.createQuery("""
                    UPDATE LotteryDraw ld
                    SET ld.totalTickets = ld.totalTickets - 1
                    WHERE ld.id = :id AND ld.totalTickets > 0
                    """);

            query.setParameter("id", id);

            int updated = query.executeUpdate();

            if (updated == 0) {
                throw new RuntimeException("Cannot decrement tickets: " + id);
            }

            tx.commit();
            logger.info("Decrement tickets: {}", id);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    private void executeTicketUpdate(UUID id, int delta) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Query<?> query = session.createQuery("""
                    UPDATE LotteryDraw ld
                    SET ld.totalTickets = ld.totalTickets + :delta
                    WHERE ld.id = :id
                    """);

            query.setParameter("delta", delta);
            query.setParameter("id", id);

            int updated = query.executeUpdate();

            if (updated == 0) {
                throw new RuntimeException("Lottery draw not found: " + id);
            }

            tx.commit();
            logger.info("Tickets updated: {} ({})", id, delta);

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

            LotteryDraw draw = session.get(LotteryDraw.class, id);
            if (draw == null) {
                throw new RuntimeException("Lottery draw not found: " + id);
            }

            session.remove(draw);

            tx.commit();
            logger.info("Lottery draw deleted: {}", id);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    // EXISTS
    @Override
    public boolean existsByDrawNumber(Integer drawNumber) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(ld) FROM LotteryDraw ld WHERE ld.drawNumber = :num",
                            Long.class
                    ).setParameter("num", drawNumber)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }

    @Override
    public boolean existsByDrawName(String drawName) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(ld) FROM LotteryDraw ld WHERE ld.drawName = :name",
                            Long.class
                    ).setParameter("name", drawName)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }
}