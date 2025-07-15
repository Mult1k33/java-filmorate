package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.utils.UserValidate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserValidate userValidate;
    private final FriendshipStorage friendshipStorage;

    // Получение всех пользователей
    public Collection<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    // Получение пользователя по id
    public UserDto getById(Long userId) {
        return userStorage.findById(userId)
                .map(UserMapper::mapToDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    // Добавление пользователя
    public UserDto create(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        userValidate.validateUser(user);

        User createdUser = userStorage.create(user);
        return UserMapper.mapToDto(createdUser);
    }

    // Обновление пользователя
    public UserDto update(UpdateUserRequest request) {
        User user = UserMapper.mapToUser(request);
        userValidate.validateUser(user);

        User oldUser = userStorage.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Пользователь с id:{} не найден", request.getId());
                    return new NotFoundException("Пользователь не найден");
                });
        user = UserMapper.updateUserFields(oldUser, request);
        return UserMapper.mapToDto(userStorage.update(user));
    }

    // Удаление пользователя
    public void delete(Long userId) {
        userStorage.delete(userId);
    }

    // Метод добавления пользователей в друзья
    public void addFriend(Long userId, Long friendId) {
        final User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка несуществующего пользователя с Id:{} добавить в кого-то в друзья", userId);
                    throw new NotFoundException("Пользователь не найден");
                });

        final User friend = userStorage.findById(friendId)
                .orElseThrow(() -> {
                    log.warn("Попытка добавить в друзья несуществующего пользователя с Id:{}", friendId);
                    throw new NotFoundException("Пользователь не найден");
                });

        if (user.equals(friend)) {
            log.warn("Попытка добавить самого себя в друзья");
            throw new DuplicateException("Нельзя добавить самого себя в друзья");
        }

        if (user.getFriends().contains(friendId)) {
            log.warn("Попытка пользователя с Id:{} снова добавить в друзья пользователя с Id:{}", userId, friendId);
            throw new DuplicateException("Пользователь уже отправлял запрос на дружбу");
        }
        user.getFriends().add(friendId);
        userStorage.update(user);

        friendshipStorage.addFriend(userId, friendId);
        log.debug("Пользователи с Id:{} и Id:{} теперь друзья", userId, friendId);
    }

    // Метод удаления пользователей из друзей
    public void removeFriend(Long userId, Long friendId) {
        final User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка несуществующего пользователя с Id:{} удалить кого-то из друзей", userId);
                    throw new NotFoundException("Пользователь не найден");
                });

        final User friend = userStorage.findById(friendId)
                .orElseThrow(() -> {
                    log.warn("Попытка удалить из друзей несуществующего пользователя с Id:{}", friendId);
                    throw new NotFoundException("Пользователь не найден");
                });

        if (user.equals(friend)) {
            log.warn("При удалении из друзей были переданы одинаковые Id: userId={}, friendId={}", userId, friendId);
            throw new DuplicateException("Нельзя удалить самого себя из друзей");
        }

        if (!user.getFriends().contains(friendId)) {
            log.warn("Пользователь с Id:{} не добавлял в друзья пользователя с Id:{}", userId, friendId);
            return;
        }

        user.getFriends().remove(friendId);
        userStorage.update(user);

        friendshipStorage.removeFriend(userId, friendId);
        log.debug("Пользователи с Id:{} и Id:{} больше не друзья", userId, friendId);
    }

    // Метод получения списка всех друзей пользователя
    public Collection<UserDto> findAllFriends(Long userId) {
        final User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка получить друзей несуществующего пользователя c Id:{}", userId);
                    throw new NotFoundException("Пользователь не найден");
                });

        // Возвращаем пустую коллекцию, если у пользователя нет друзей
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            log.debug("У пользователя с Id:{} нет друзей", userId);
            return Collections.emptyList();
        }

        return userStorage.findAll().stream()
                .filter(u -> user.getFriends().contains(u.getId()))
                .map(UserMapper::mapToDto)
                .toList();
    }

    // Метод получения общих друзей
    public Collection<UserDto> findCommonFriends(Long userId, Long friendId) {
        final User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка получить общих друзей у несуществующего пользователя c Id:{}", userId);
                    throw new NotFoundException("Пользователь не найден");
                });

        final User friend = userStorage.findById(friendId)
                .orElseThrow(() -> {
                    log.warn("Попытка получить общих друзей с несуществующим пользователем с Id:{}",friendId);
                    throw new NotFoundException("Пользователь не найден");
                });

        // Поиск пересечения друзей
        Set<Long> intersection = new HashSet<>(user.getFriends());
        intersection.retainAll(friend.getFriends());

        if (intersection.isEmpty()) {
            log.debug("У пользователей {} и {} нет общих друзей", userId, friendId);
        }

        return userStorage.findAll().stream()
                .filter(u -> intersection.contains(u.getId()))
                .map(UserMapper::mapToDto)
                .toList();
    }
}