package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger("FilmController");
    private final FilmStorage memFilmStorage;
    private final UserStorage memUserStorage;
    private final Map<Integer, Film> films = memFilmStorage.getFilms();
    private final Map<Integer, User> users = memUserStorage.getUsers();
      public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        checkRules(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Пользователь выбрал {}", film.getName());
        return film;
    }

    public Film getById(Integer id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
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
            throw new NotFoundException(String.format("Фильм с id %s не найден", filmId));
        }
        if (!users.containsKey(userId)) {
            throw new NotFoundException(String.format("Пользователь с id %s не найден",
                    userId));
        }
        films.get(filmId).getLikes().add(userId);
        return films.get(filmId);
    }

    public void removeLike(Integer userId, Integer filmId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException(String.format("Фильм с Id %s не найден", filmId));
        }
        Set<Integer> likes = films.get(filmId).getLikes();
        if (!likes.contains(userId)) {
            throw new NotFoundException(String.format("Пользователь c ID %s не оценивал этот фильм", userId));
        }
        likes.remove(userId);
    }

    public List<Film> getTopFilms(int count) {
          if (count < 0) {
              throw new ValidationException("Некорректное значение count");
          }
          if (count > films.size()) {
              count = films.size();
          }
          return films.values().stream()
                .sorted((film1, film2)-> film2.getLikes().size() - film1.getLikes().size())
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
