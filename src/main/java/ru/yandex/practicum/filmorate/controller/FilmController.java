package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger("FilmController");

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        checkRules(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Пользователь выбрал {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Не указан Id");
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
        log.warn("Фильм с id {} не найден", newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    public void checkRules(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Не указано наименование фильма");
            throw new ValidationException("Наименование не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Описание более 200 символов");
            throw new ValidationException("Описание не может быть более 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Некоректная дата релиза");
            throw new ValidationException("В это время еще не было кинематографа");
        }
        if (film.getDuration() <= 0) {
            log.warn("Некоректная продолжительность фильма");
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
