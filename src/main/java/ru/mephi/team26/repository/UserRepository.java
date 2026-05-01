package ru.mephi.team26.repository;

import org.hibernate.query.Query;
import ru.mephi.team26.entity.User;
import ru.mephi.team26.util.HibernateUtil;

import java.util.Optional;

public class UserRepository {

    public User save(User user) {
        return HibernateUtil.inTransaction(session -> {
            session.persist(user);
            return user;
        });
    }

    // unused
    public Optional<User> findById(Long id) {
        return HibernateUtil.inTransaction(session -> Optional.ofNullable(session.get(User.class, id)));
    }

    public Optional<User> findByUsername(String username) {
        return HibernateUtil.inTransaction(session -> {
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResultOptional();
        });
    }
}