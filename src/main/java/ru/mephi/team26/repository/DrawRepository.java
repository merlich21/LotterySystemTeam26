package ru.mephi.team26.repository;

import io.javalin.http.ConflictResponse;
import io.javalin.http.NotFoundResponse;
import org.hibernate.LockMode;
import org.hibernate.query.Query;
import ru.mephi.team26.entity.*;
import ru.mephi.team26.util.GeneratorUtil;
import ru.mephi.team26.util.HibernateUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class DrawRepository {

    public Draw save(Draw draw) {
        return HibernateUtil.inTransaction(session -> {
            session.persist(draw);
            return draw;
        });
    }

    public Optional<Draw> findById(Long id) {
        return HibernateUtil.inTransaction(session -> Optional.ofNullable(session.get(Draw.class, id)));
    }

    public List<Draw> findActive() {
        return HibernateUtil.inTransaction(session -> {
            Query<Draw> query = session.createQuery("FROM Draw WHERE status = :status ORDER BY id DESC", Draw.class);
            query.setParameter("status", DrawStatus.ACTIVE);
            return query.list();
        });
    }

    public Draw completeById(Long id) {
        return HibernateUtil.inTransaction(session -> {
            Draw draw = session.get(Draw.class, id, LockMode.PESSIMISTIC_WRITE);
            if (draw == null) {
                throw new NotFoundResponse("Draw with id " + id + " was not found");
            }
            if (draw.getStatus() != DrawStatus.ACTIVE) {
                throw new ConflictResponse("Draw with id " + id + " is not active");
            }

            List<Integer> winningNumbers = GeneratorUtil.generateWinningNumbers(draw);

            DrawResult drawResult = new DrawResult();
            drawResult.setDraw(draw);
            drawResult.setWinningNumbers(winningNumbers);
            drawResult.setGeneratedAt(OffsetDateTime.now(ZoneOffset.UTC));
            session.persist(drawResult);

            draw.setStatus(DrawStatus.COMPLETED);
            session.merge(draw);

            Query<Ticket> query = session.createQuery("FROM Ticket WHERE draw = :draw", Ticket.class);
            query.setParameter("draw", draw);
            List<Ticket> tickets = query.list();

            for (Ticket ticket : tickets) {
                TicketStatus status = ticket.getNumbers().equals(winningNumbers) ? TicketStatus.WIN : TicketStatus.LOSE;
                ticket.setStatus(status);
                session.merge(ticket);
            }
            return draw;
        });
    }
}