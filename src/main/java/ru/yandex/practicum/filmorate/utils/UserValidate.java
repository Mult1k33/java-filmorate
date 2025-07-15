package ru.yandex.practicum.filmorate.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

/**
 * Утилитарный класс для валидации объектов типа User
 */

@Slf4j
@Component
public class UserValidate {

    // Вспомогательный метод проверки выполнения необходимых условий
    public static void validateUser(User user) {

        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            log.error("Попытка ввести некорректный логин");
            throw new ValidationException("Логин не должен быть пустым или содержать пробелов");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем:{}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getBirthday() == null) {
            log.error("Попытка ввести пустую дату рождения");
            throw new ValidationException("Дата рождения не может быть null");
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
}