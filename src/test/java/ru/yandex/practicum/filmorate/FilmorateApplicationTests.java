package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmorateApplicationTests {
    FilmController filmController;
    UserController userController;
    FilmService filmService;
    UserService userService;
    UserStorage userStorage;
    FilmStorage filmStorage;

    @BeforeEach
    void createControllers() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(filmStorage, userStorage);
        userService = new UserService(userStorage);
        //filmController = new FilmController(filmService);
        //userController = new UserController();

    }

    @Test
    void wrongRealiseData() {
        final Film film = new Film(null, "Интересный фильм", "Описание фильма",
                LocalDate.of(1800, 1, 1), 100);
        assertThrows(ValidationException.class, () ->
                        filmController.create(film),
                "Необрабатывается некорректная дата релиза");
    }

    @Test
    void wrongDescription() {
        final Film film = new Film(null, "Интересный фильм", "Очень длинное описание фильмааааааааааа" +
                "ааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа" +
                "ааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа" +
                "ааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа" +
                "ааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа",
                LocalDate.of(1954, 1, 1), 100);
        assertThrows(ValidationException.class, () ->
                        filmController.create(film),
                "Необрабатывается некорректное описание");
    }

    @Test
    void emptyName() {
        final Film film = new Film(null, "", "Описание фильма",
                LocalDate.of(2016, 1, 1), 100);
        assertThrows(ValidationException.class, () ->
                        filmController.create(film),
                "Необрабатывается отсутствие наименования");
    }

    @Test
    void wrongDuration() {
        final Film film = new Film(null, "", "Описание фильма",
                LocalDate.of(2016, 1, 1), -200);
        assertThrows(ValidationException.class, () ->
                        filmController.create(film),
                "Необрабатывается некорректная продолжительность");
    }

    @Test
    void wrongEmail() {
        User user = new User(null, "18ya", "login", "Петр",
                LocalDate.of(1990, 10, 8));
        assertThrows(ValidationException.class, () ->
                        userController.create(user),
                "Необрабатывается некорректный имайл");
    }

    @Test
    void emptyEmail() {
        User user = new User(null, null, "login", "Петр",
                LocalDate.of(1990, 10, 8));
        assertThrows(ValidationException.class, () ->
                        userController.create(user),
                "Необрабатывается пустой имайл");
    }

    @Test
    void emptyLogin() {
        User user = new User(null, "123@ya.ru", "", "Петр",
                LocalDate.of(1990, 10, 8));
        assertThrows(ValidationException.class, () ->
                        userController.create(user),
                "Необрабатывается пустой логин");
    }

    @Test
    void wrongBirthday() {
        User user = new User(null, "123@ya.ru", "login", "Петр",
                LocalDate.of(2120, 10, 8));
        assertThrows(ValidationException.class, () ->
                        userController.create(user),
                "Необрабатывается некорректная дата рождения");
    }
}

