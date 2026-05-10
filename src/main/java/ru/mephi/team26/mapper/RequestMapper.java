package ru.mephi.team26.mapper;

public interface RequestMapper<E, R> {

    E requestDtoToEntity(R requestDto);
}
