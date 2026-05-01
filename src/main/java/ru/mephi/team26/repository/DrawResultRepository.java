package ru.mephi.team26.repository;

import ru.mephi.team26.entity.DrawResult;
import ru.mephi.team26.util.HibernateUtil;

import java.util.Optional;

public class DrawResultRepository {

    public Optional<DrawResult> findResult(long drawId) {
        return HibernateUtil.inTransaction(session -> Optional.ofNullable(session.get(DrawResult.class, drawId)));
    }
}
