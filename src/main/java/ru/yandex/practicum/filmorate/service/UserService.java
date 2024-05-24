package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger("UserController");
    private final UserStorage memUserStorage;
    private final Map<Integer, User> users = memUserStorage.getUsers();

    public Collection<User> findAll() {
        return users.values();
    }


    public User create(User user) {
        checkRules(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Зарегистрировался пользователь с логином {}", user.getLogin());
        return user;
    }


    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("Не указан Id пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            checkRules(newUser);
            oldUser.setName(newUser.getName());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());
            log.info("Изменены данные пользователя с Id {}", oldUser.getId());
            return oldUser;
        }
        log.error("Пользователь с Id {} не найден", newUser.getId());
        throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    public User getById(Integer id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new ValidationException(String.format("Пользователь № %s не найден", id));
    }

    public User addFriend(Integer id, Integer friendId) {
        if (!users.containsKey(id)) {
            throw new ValidationException(String.format("Пользователь № %s не найден", id));
        }
        if (!users.containsKey(friendId)) {
            throw new ValidationException(String.format("Пользователь № %s найден и не может быть добавлен в друзья",
                    id));
        }
        users.get(id).getFriends().add(friendId);
        users.get(friendId).getFriends().add(id);
        return users.get(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        if (!users.containsKey(id)) {
            throw new ValidationException(String.format("Пользователь № %s не найден", id));
        }
        Set<Integer> friends = users.get(id).getFriends();
        if (!friends.contains(friendId)) {
            throw new ValidationException(String.format("Пользователь c ID %s не найден в списке друзей", friendId));
        }
        friends.remove(friendId);
    }

    public List<String> getCommonFriends(Integer userId1, Integer userId2) {
        List<String> commonFriends = new ArrayList<>();
        for (Integer id1 : users.get(userId1).getFriends()) {
            for (Integer id2 : users.get(userId2).getFriends()) {
                if (id1 == id2) {
                    String friend = String.format("%s %s", id2, users.get(userId2).getName());
                    commonFriends.add(friend);
                }
            }
        }
        return commonFriends;
    }

    public void checkRules(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Некорректный имайл");
            throw new ValidationException("Имайл не указан или некорректен");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Некорректный логин");
            throw new ValidationException("Логин не указан или содержит пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, всместо имени использован логин {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Некоректная дата рождения");
            throw new ValidationException("Некоректная дата рождения");
        }
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
