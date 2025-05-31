package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static ru.yandex.practicum.filmorate.utils.ControllersUtils.getNextId;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на добавление нового пользователя {}", newUser.getLogin());

        if (newUser == null) {
            log.error("Попытка добавить null");
            throw new NullPointerException("Пользователь не может быть null");
        }

        checkEmailUniqueness(newUser.getEmail());

        validateUser(newUser);
        userEmails.add(newUser.getEmail().toLowerCase());
        newUser.setId(getNextId(users.keySet()));

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь {} успешно добавлен", newUser.getName());
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с id:{}", user.getId());

        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id:{} не найден", user.getId());
            throw new NotFoundException("Пользователь с заданным id не существует");
        }

        validateUser(user);

        // Случай, когда при обновлении меняется email и нужно удалить старый из коллекции email-ов
        final User oldUser = users.get(user.getId());
        if (!oldUser.getEmail().equalsIgnoreCase(user.getEmail())) {
            userEmails.remove(oldUser.getName().toLowerCase());
            userEmails.add(user.getEmail().toLowerCase());
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь {} c id:{} успешно обновлен", user.getName(), user.getId());
        return user;
    }

    // Вспомогательный метод проверки выполнения необходимых условий
    private void validateUser(User user) {

        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            log.error("Попытка ввести некорректный логин");
            throw new ValidationException("Логин не должен быть пустым или содержать пробелов");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем:{}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getEmail().isEmpty()) {
            log.error("Попытка добавить пустой email");
            throw new ValidationException("email не может быть пустым");
        }

        if (!user.getEmail().contains("@")) {
            log.error("Попытка добавить некорректный email");
            throw new ValidationException("email не содержит @");
        }
    }

    // Вспомогательный метод для проверки на наличие дубликата email
    private void checkEmailUniqueness(String email) {
        if (userEmails.contains(email.toLowerCase())) {
            log.warn("Пользователь с email:{} уже был добавлен", email);
            throw new DuplicateException("Email уже используется");
        }
    }
}