package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger("FilmController");
    private final FilmStorage inMemoryFilmStorage;
    private final UserStorage inMemoryUserStorage;

    private Map<Integer, Film> getFilms() {
        return inMemoryFilmStorage.getFilms();
    }

    public Collection<Film> findAll() {
        return getFilms().values();
    }

    public Film create(Film film) {
        checkRules(film);
        film.setId(getNextId());
        getFilms().put(film.getId(), film);
        log.info("Пользователь добавил {}", film.getName());
        return film;
    }

    public Film getById(Integer id) {
        if (getFilms().containsKey(id)) {
            log.info("Пользователь выбрал фильм {}", getFilms().get(id).getName());
            return getFilms().get(id);
        }
        log.error("Фильм Id {} не найден", id);
        throw new NotFoundException(String.format("Фильм Id № %s не найден", id));
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Не указан Id");
            throw new ValidationException("Id должен быть указан");
        }
        if (getFilms().containsKey(newFilm.getId())) {
            Film oldFilm = getFilms().get(newFilm.getId());
            checkRules(newFilm);
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Пользователь изменил данные фильма в Id {}", oldFilm.getId());
            return oldFilm;
        }
        log.error("Фильм с id {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    public Film addLike(Integer filmId, Integer userId) {
        Map<Integer, User> users = inMemoryUserStorage.getUsers();
        if (!getFilms().containsKey(filmId)) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException(String.format("Фильм с id %s не найден", filmId));
        }
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id %s не найден", userId));
        }
        Set<Integer> likes = getFilms().get(filmId).getLikes();
        likes.add(userId);
        log.info("Добавлен лайк к фильму с id {}", filmId);
        return getFilms().get(filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!getFilms().containsKey(filmId)) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException(String.format("Фильм с Id %s не найден", filmId));
        }
        Set<Integer> likes = getFilms().get(filmId).getLikes();
        if (!likes.contains(userId)) {
            log.error("Неверный id {} пользователя", userId);
            throw new NotFoundException(String.format("Пользователь c ID %s не оценивал этот фильм", userId));
        }
        likes.remove(userId);
        log.info("Удален лайк к фильму с id {}", filmId);
    }

    public List<Film> getTopFilms(int count) {
        if (count < 0) {
            log.error("Некорректное количество {}", count);
            throw new ValidationException("Некорректное значение count");
        }
        if (count > getFilms().size()) {
            count = getFilms().size();
        }
        log.info("Подборка {} популярных фильмов", count);
        return getFilms().values().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void checkRules(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Не указано наименование фильма");
            throw new ValidationException("Наименование не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.error("Описание более 200 символов");
            throw new ValidationException("Описание не может быть более 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Некоректная дата релиза");
            throw new ValidationException("В это время еще не было кинематографа");
        }
        if (film.getDuration() <= 0) {
            log.error("Некоректная продолжительность фильма");
            throw new ValidationException("Продолжительность фильма должна быть больше нуля");
        }
    }

    private int getNextId() {
        int currentMaxId = getFilms().keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
