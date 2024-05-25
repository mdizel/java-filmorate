package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger("UserController");
    @Getter
    private final UserStorage memUserStorage = new InMemoryUserStorage();
    @Getter
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
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    public User getById(Integer id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new NotFoundException(String.format("Пользователь № %s не найден", id));
    }

    public User addFriend(Integer id, Integer friendId) {
        if (!users.containsKey(id)) {
            log.error("Пользователь с Id {} не найден", id);
            throw new NotFoundException(String.format("Пользователь № %s не найден", id));
        }
        if (!users.containsKey(friendId)) {
            log.error("Друг с Id {} не найден", friendId);
            throw new NotFoundException(String.format("Пользователь № %s не найден и не может быть добавлен в друзья",
                    friendId));
        }
        Set<Integer> friends = users.get(id).getFriends();
        friends.add(friendId);
        users.get(id).setFriends(friends);
        Set<Integer> fFriends = users.get(friendId).getFriends();
        fFriends.add(id);
        users.get(friendId).setFriends(fFriends);
        log.info("Друг с Id {} добавлен", friendId);
        return users.get(id);
    }

    public void removeFriend(Integer id, Integer friendId) {
        if (!users.containsKey(id)) {
            log.error("Пользователь с Id {} не найден", id);
            throw new NotFoundException(String.format("Пользователь № %s не найден", id));
        }
        Set<Integer> friends = users.get(id).getFriends();
        if (!friends.contains(friendId)) {
            log.error("Друг с Id {} не найден", friendId);
            throw new NotFoundException(String.format("Пользователь c ID %s не найден в списке друзей", friendId));
        }
        friends.remove(friendId);
        users.get(id).setFriends(friends);
        Set<Integer> fFriends = users.get(friendId).getFriends();
        log.info("Пользователь с Id {} удален из друзей", friendId);
        fFriends.remove(id);
        users.get(friendId).setFriends(fFriends);
    }

    public List<User> getFriends(Integer userId) {
        if (!users.containsKey(userId)) {
            log.error("Друг с Id {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь № %s не найден", userId));
        }
        log.info("Получен список друзей пользователя {}", userId);
        return users.get(userId).getFriends().stream()
                .map(friendId -> users.get(friendId))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        if (!users.containsKey(userId1)) {
            log.error("Пользователь с Id {} не найден", userId1);
            throw new NotFoundException(String.format("Пользователь № %s не найден", userId1));
        }
        if (!users.containsKey(userId2)) {
            log.error("Пользователь2 с Id {} не найден", userId2);
            throw new NotFoundException(String.format("Пользователь2 № %s не найден", userId2));
        }
        List<User> commonFriends = new ArrayList<>();
        for (Integer id1 : users.get(userId1).getFriends()) {
            for (Integer id2 : users.get(userId2).getFriends()) {
                if (id1.equals(id2)) {
                    commonFriends.add(users.get(userId2));
                }
            }
        }
        log.info("Получен список общих друзей пользователей {} и {}", userId1, userId2);
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
