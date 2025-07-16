package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static ru.yandex.practicum.filmorate.utils.ControllersUtils.getNextId;

/**
 * Утилитарный класс - тестовое хранилище для User
 */

@Slf4j
@Repository
@Primary
public class UserTestStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> usersEmail = new HashSet<>();

    // Добавление пользователя
    @Override
    public User create(User user) {

        checkEmailUniqueness(user.getEmail());

        if (user.getName() == null || user.getName().isEmpty()) {
            log.info("Имя пользователя не задано. Вместо имени задается логин");
            user.setName(user.getLogin());
        }

        usersEmail.add(user.getEmail().toLowerCase());
        user.setId(getNextId(users.keySet()));
        user.setFriends(new HashSet<>());
        users.put(user.getId(), user);
        return user;
    }

    // Удаление пользователя по id
    @Override
    public void delete(Long id) {
        validateUserId(id);

        usersEmail.remove(users.get(id).getEmail().toLowerCase());
        users.remove(id);
    }

    // Изменение пользователя
    @Override
    public User update(User user) {
        validateUserId(user.getId());

        final User oldUser = users.get(user.getId());

        // Случай, когда при обновлении меняется email и нужно удалить старый из коллекции email-ов
        if (!oldUser.getEmail().equalsIgnoreCase(user.getEmail())) {
            usersEmail.remove(oldUser.getEmail().toLowerCase());
            usersEmail.add(user.getEmail().toLowerCase());
        }

        users.put(user.getId(), user);
        return user;
    }

    // Получение пользователя по id
    @Override
    public Optional<User> findById(Long id) {
        validateUserId(id);
        return Optional.ofNullable(users.get(id));
    }

    // Получение всех пользователей
    @Override
    public Collection<User> findAll() {
        return List.copyOf(users.values());
    }

    // Вспомогательный метод для проверки на наличие дубликата email
    private void checkEmailUniqueness(String email) {
        if (usersEmail.contains(email.toLowerCase())) {
            log.warn("Email '{}' уже занят", email);
            throw new DuplicateException("Email уже используется");
        }
    }

    // Вспомогательный метод для валидации пользователя
    private void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Идентификатор пользователя должен быть определён и положительным");
        }

        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }
}