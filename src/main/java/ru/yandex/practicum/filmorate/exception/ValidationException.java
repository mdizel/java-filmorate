package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends RuntimeException {
    String message;

    public ValidationException(String message) {
        super(message);
    }
}
