package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendshipStorage {

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Collection<User> findAllFriends(Long userId);

    Collection<User> findCommonFriends(Long userId, Long friendId);
}