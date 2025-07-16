package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FriendshipDbStorage extends BaseDbStorage<Friendship> implements FriendshipStorage {

    private final UserDbStorage userStorage;

    private static final String INSERT_QUERY = "INSERT INTO friendship(user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT f.user_id, f.friend_id FROM friendship f WHERE f.user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT f1.user_id, f1.friend_id " +
                    "FROM friendship f1 " +
                    "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ?";

    public FriendshipDbStorage(JdbcTemplate jdbc, RowMapper<Friendship> mapper, UserDbStorage userStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
    }

    // Добавление друга
    @Override
    public void addFriend(Long userId, Long friendId) {
        update(INSERT_QUERY, userId, friendId);
    }

    // Удаление друга
    @Override
    public void removeFriend(Long userId, Long friendId) {
        update(DELETE_QUERY, userId, friendId);
    }

    // Получение всех друзей пользователя
    @Override
    public Collection<User> findAllFriends(Long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId).stream()
                .map(friendship -> userStorage.findById(friendship.getFriendId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // Получение общих друзей
    @Override
    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId).stream()
                .map(friendship -> userStorage.findById(friendship.getFriendId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}