package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private Map<Integer, User> users = new HashMap<>();

    @Override
    public Map<Integer, User> getUsers() {
        return users;
    }
}
