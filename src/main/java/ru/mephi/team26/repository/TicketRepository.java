package ru.mephi.team26.repository;

import org.hibernate.query.Query;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.entity.Ticket;
import ru.mephi.team26.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class TicketRepository {

    public Ticket save(Ticket ticket) {
        return HibernateUtil.inTransaction(session -> {
            session.persist(ticket);
            return ticket;
        });
    }

    public Optional<Ticket> findById(Long id) {
        return HibernateUtil.inTransaction(session -> Optional.ofNullable(session.get(Ticket.class, id)));
    }

    // unused
    public List<Ticket> findAllByDraw(Draw draw) {
        return HibernateUtil.inTransaction(session -> {
            Query<Ticket> query = session.createQuery("FROM Ticket WHERE draw = :draw ORDER BY id", Ticket.class);
            query.setParameter("draw", draw);
            return query.list();
        });
    }
}