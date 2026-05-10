package ru.mephi.team26.validator;

public interface Validator<T> {

    void validate(T dto, Object... args);
}
