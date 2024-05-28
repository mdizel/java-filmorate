package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Integer, Film> films = new HashMap<>();

    @Override
    public Map<Integer, Film> getFilms() {
        return films;
    }
}
