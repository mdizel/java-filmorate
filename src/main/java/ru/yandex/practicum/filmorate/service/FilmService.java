package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger("FilmController");
    private final FilmStorage memFilmStorage = new InMemoryFilmStorage();
    private final Map<Integer, Film> films = memFilmStorage.getFilms();

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        checkRules(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Пользователь добавил {}", film.getName());
        return film;
    }

    public Film getById(Integer id) {
        if (films.containsKey(id)) {
            log.info("Пользователь выбрал фильм {}", films.get(id).getName());
            return films.get(id);
        }
        log.error("Фильм Id {} не найден",id);
        throw new NotFoundException(String.format("Фильм Id № %s не найден", id));
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Не указан Id");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
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
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException(String.format("Фильм с id %s не найден", filmId));
        }
        Set<Integer> likes = films.get(filmId).getLikes();
        likes.add(userId);
        films.get(filmId).setLikes(likes);
        log.info("Добавлен лайк к фильму с id {}", filmId);
        return films.get(filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException(String.format("Фильм с Id %s не найден", filmId));
        }
        Set<Integer> likes = films.get(filmId).getLikes();
        if (!likes.contains(userId)) {
            log.error("Неверный id {} пользователя", userId);
            throw new NotFoundException(String.format("Пользователь c ID %s не оценивал этот фильм", userId));
        }
        likes.remove(userId);
        log.info("Удален лайк к фильму с id {}", filmId);
        films.get(filmId).setLikes(likes);
    }

    public List<Film> getTopFilms(int count) {
        if (count < 0) {
            log.error("Некорректное количество {}", count);
            throw new ValidationException("Некорректное значение count");
        }
        if (count > films.size()) {
            count = films.size();
        }
        log.info("Подборка {} популярных фильмов", count);
        return films.values().stream()
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
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
