package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.utils.UserValidate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserValidate userValidate;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        userValidate.validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        userValidate.validateUser(user);
        return userStorage.update(user);
    }

    public void delete(Long id) {
        userStorage.delete(id);
    }

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    // Метод добавления пользователей в друзья
    public void addFriend(Long userId, Long friendId) {
        final User user = userStorage.getById(userId);
        if (user == null) {
            log.warn("Попытка несуществующего пользователя с Id:{} добавить в друзья пользователей", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        final User friend = userStorage.getById(friendId);
        if (friend == null) {
            log.warn("Попытка добавить в друзья несуществующего пользователя с Id:{}", friendId);
            throw new NotFoundException("Пользователь не найден");
        }

        if (user.equals(friend)) {
            log.warn("Попытка добавить самого себя в друзья");
            throw new DuplicateException("Нельзя добавить самого себя в друзья");
        }

        Set<Long> userFriends = user.getFriends();
        if (userFriends.contains(friendId)) {
            log.debug("Пользователь с Id:{} уже добавил в друзья пользователя с Id:{}", userId, friendId);
            return;
        }

        userFriends.add(friendId);
        user.setFriends(userFriends);
        userStorage.update(user);

        Set<Long> friendFriends = friend.getFriends();
        friendFriends.add(userId);
        friend.setFriends(friendFriends);
        userStorage.update(friend);
        log.debug("Пользователи с Id:{} и Id:{} теперь друзья", userId, friendId);
    }

    // Метод удаления пользователей из друзей
    public void removeFriend(Long userId, Long friendId) {
        final User user = userStorage.getById(userId);
        if (user == null) {
            log.warn("Попытка несуществующего пользователя с Id:{} удалить кого-то из друзей", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        final User friend = userStorage.getById(friendId);
        if (friend == null) {
            log.warn("Попытка удалить из друзей несуществующего пользователя с Id:{}", friendId);
            throw new NotFoundException("Пользователь не найден");
        }

        if (user.equals(friend)) {
            log.warn("При удалении из друзей были переданы одинаковые Id: userId={}, friendId={}", userId, friendId);
            throw new DuplicateException("Нельзя удалить самого себя из друзей");
        }

        Set<Long> userFriends = user.getFriends();
        if (!userFriends.contains(friendId)) {
            log.debug("Пользователь с Id:{} не добавлял в друзья пользователя с Id:{}", userId, friendId);
            return;
        }

        userFriends.remove(friendId);
        user.setFriends(userFriends);
        userStorage.update(user);

        Set<Long> friendFriends = friend.getFriends();
        friendFriends.remove(userId);
        friend.setFriends(friendFriends);
        userStorage.update(friend);
        log.debug("Пользователи с Id:{} и Id:{} больше не друзья", userId, friendId);
    }

    // Метод получения списка всех друзей пользователя
    public Collection<User> getFriends(Long id) {
        final User user = userStorage.getById(id);

        if (user == null) {
            log.warn("Попытка получить друзей несуществующего пользователя c Id:{}", id);
            throw new NotFoundException("Пользователь не найден");
        }

        // Возвращаем пустую коллекцию, если у пользователя нет друзей
        if (user.getFriends().isEmpty()) {
            log.debug("У пользователя с Id:{} нет друзей", id);
            return Collections.emptyList();
        }

        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    // Метод получения общих друзей
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        final User user = getById(userId);
        if (user == null) {
            log.warn("Попытка получить общих друзей у несуществующего пользователя c Id:{}", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        final User friend = getById(friendId);
        if (friend == null) {
            log.warn("Попытка получить общих друзей с несуществующим пользователем с Id:{}",friendId);
            throw new NotFoundException("Пользователь не найден");
        }

        // Поиск пересечения друзей
        Set<Long> intersection = new HashSet<>(user.getFriends());
        intersection.retainAll(friend.getFriends());

        if (intersection.isEmpty()) {
            log.debug("У пользователей {} и {} нет общих друзей", userId, friendId);
        }

        return userStorage.findAll().stream()
                .filter(u -> intersection.contains(u.getId()))
                .toList();
    }
}