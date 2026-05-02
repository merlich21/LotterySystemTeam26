package team26.repository.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team26.domain.user.User;
import team26.domain.user.UserRole;
import team26.util.database.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateUserRepository implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUserRepository.class);
    private final SessionFactory sessionFactory;

    public HibernateUserRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // CREATE
    @Override
    public User save(User user) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            session.persist(user);

            tx.commit();
            logger.info("User saved: {}", user.getId());
            return user;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error saving user: {}", user.getLogin(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    // READ
    @Override
    public Optional<User> findByLogin(String login) {
        if (login == null) return Optional.empty();

        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.login = :login", User.class);
            query.setParameter("login", login);

            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) return Optional.empty();

        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User", User.class).list();
        }
    }

    @Override
    public List<User> findAllByRole(UserRole role) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.role = :role", User.class);
            query.setParameter("role", role);

            return query.list();
        }
    }

    // UPDATE
    @Override
    public User update(User user) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            User merged = (User) session.merge(user);

            tx.commit();
            logger.info("User updated: {}", user.getId());

            return merged;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating user: {}", user.getId(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    // DELETE
    @Override
    public void delete(UUID id) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user == null) {
                throw new RuntimeException("User not found for delete: " + id);
            }

            session.remove(user);

            tx.commit();
            logger.info("User deleted: {}", id);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error deleting user: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    // EXISTS
    @Override
    public boolean existsByLogin(String login) {
        if (login == null) return false;

        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class);
            query.setParameter("login", login);

            return query.uniqueResult() > 0;
        }
    }
}