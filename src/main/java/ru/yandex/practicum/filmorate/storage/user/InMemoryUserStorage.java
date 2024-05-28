package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository

public class InMemoryUserStorage implements UserStorage {
    private Map<Integer, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger("InMemoryUserStorage");

    @Override
    public Map<Integer, User>  getUsers() {
        //List<User> userList = new ArrayList<>(users.values());
        return users;
    }

    public void checkUserId(Integer userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с Id {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь № %s не найден", userId));
        }
    }
}
