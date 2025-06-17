package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User create(User user);

    void delete(Long id);

    User update(User user);

    User getById(Long id);

    Collection<User> findAll();
}