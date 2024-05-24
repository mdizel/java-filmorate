package ru.yandex.practicum.filmorate.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound (final NotFoundException e) {
        return new ErrorResponse("Объект не найден", e.getMessage());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleConditionsNotMet (final ValidationException e) {
        return new ErrorResponse("Некорректные данные", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedError (final Throwable e) {
        return new ErrorResponse("Ошибка", "Произошла непредвиденная ошибка.");
    }
}
