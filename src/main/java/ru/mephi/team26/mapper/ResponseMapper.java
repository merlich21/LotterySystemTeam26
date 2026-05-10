package ru.mephi.team26.mapper;

public interface ResponseMapper<E, R> {

    R entityToResponseDto(E entity);
}
