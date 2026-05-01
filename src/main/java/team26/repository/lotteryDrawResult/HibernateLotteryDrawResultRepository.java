package team26.repository.lotteryDrawResult;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.domain.lotteryDrawResult.LotteryDrawResult;
import team26.util.database.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateLotteryDrawResultRepository implements LotteryDrawResultRepository {

    private static final Logger log = LoggerFactory.getLogger(HibernateLotteryDrawResultRepository.class);
    private final SessionFactory sessionFactory;

    public HibernateLotteryDrawResultRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // CREATE
    @Override
    public LotteryDrawResult save(LotteryDrawResult result) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            session.persist(result);

            tx.commit();
            log.info("Lottery draw result saved for draw: {}", result.getLotteryDraw().getId());

            return result;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to save lottery draw result", e);
        }
    }

    // READ
    @Override
    public Optional<LotteryDrawResult> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(LotteryDrawResult.class, id));
        }
    }

    @Override
    public Optional<LotteryDrawResult> findByLotteryDrawId(UUID drawId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryDrawResult r
                    WHERE r.lotteryDraw.id = :drawId
                    """, LotteryDrawResult.class)
                    .setParameter("drawId", drawId)
                    .uniqueResultOptional();
        }
    }

    @Override
    public List<LotteryDrawResult> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    FROM LotteryDrawResult r
                    ORDER BY r.createdAt DESC
                    """, LotteryDrawResult.class)
                    .list();
        }
    }

    // UPDATE
    @Override
    public LotteryDrawResult update(LotteryDrawResult result) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            LotteryDrawResult merged = (LotteryDrawResult) session.merge(result);

            tx.commit();
            log.info("Lottery draw result updated: {}", result.getId());

            return merged;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to update lottery draw result", e);
        }
    }

    @Override
    public void updateResultNumbers(UUID id, Integer[] numbers) {
        if (numbers == null || numbers.length != 5) {
            throw new IllegalArgumentException("Result numbers must be length = 5");
        }

        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            int updated = session.createQuery("""
                    UPDATE LotteryDrawResult r
                    SET r.resultNumbers = :numbers
                    WHERE r.id = :id
                    """)
                    .setParameter("numbers", numbers)
                    .setParameter("id", id)
                    .executeUpdate();

            if (updated == 0) {
                throw new RuntimeException("Result not found: " + id);
            }

            tx.commit();
            log.info("Result numbers updated: {}", id);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to update result numbers", e);
        }
    }

    // DELETE
    @Override
    public void delete(UUID id) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            LotteryDrawResult result = session.get(LotteryDrawResult.class, id);
            if (result == null) {
                throw new RuntimeException("Result not found: " + id);
            }

            session.remove(result);

            tx.commit();
            log.info("Lottery draw result deleted: {}", id);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByLotteryDrawId(UUID drawId) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            int deleted = session.createQuery("""
                    DELETE FROM LotteryDrawResult r
                    WHERE r.lotteryDraw.id = :drawId
                    """)
                    .setParameter("drawId", drawId)
                    .executeUpdate();

            tx.commit();

            if (deleted == 0) {
                log.warn("No result found for draw: {}", drawId);
            } else {
                log.info("Deleted result for draw: {}", drawId);
            }

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
                    SELECT COUNT(r)
                    FROM LotteryDrawResult r
                    WHERE r.id = :id
                    """, Long.class)
                    .setParameter("id", id)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }

    @Override
    public boolean existsByLotteryDrawId(UUID drawId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("""
                    SELECT COUNT(r)
                    FROM LotteryDrawResult r
                    WHERE r.lotteryDraw.id = :drawId
                    """, Long.class)
                    .setParameter("drawId", drawId)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }
}