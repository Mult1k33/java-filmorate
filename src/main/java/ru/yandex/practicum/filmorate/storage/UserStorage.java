package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    void delete(Long findId);

    User update(User user);

    Optional<User> findById(Long userId);

    Collection<User> findAll();
}