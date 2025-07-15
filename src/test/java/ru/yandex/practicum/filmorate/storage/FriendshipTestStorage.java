package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

/**
 * Утилитарный класс - тестовое хранилище Friendship
 */

@Slf4j
@Repository
@Primary
public class FriendshipTestStorage implements FriendshipStorage {

    private final Map<Long, Set<Long>> friendships = new HashMap<>();
    private UserTestStorage userTestStorage;

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (!friendships.containsKey(userId)) {
            friendships.put(userId, new HashSet<>());
        }
        friendships.get(userId).add(friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // Получаем множество друзей пользователя
        Set<Long> userFriends = friendships.get(userId);

        // Если множество существует — удаляем друга
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
    }

    @Override
    public Collection<User> findAllFriends(Long userId) {
        Set<Long> friendIds = friendships.getOrDefault(userId, Set.of());
        List<User> friends = new ArrayList<>();

        for (Long friendId : friendIds) {
            userTestStorage.findById(friendId).ifPresent(friends::add);
        }

        return friends;
    }

    @Override
    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = friendships.getOrDefault(userId, Set.of());
        Set<Long> otherFriends = friendships.getOrDefault(otherId, Set.of());
        List<User> commonFriends = new ArrayList<>();

        // Пересечение множеств
        for (Long friendId : userFriends) {
            if (otherFriends.contains(friendId)) {
                userTestStorage.findById(friendId).ifPresent(commonFriends::add);
            }
        }

        return commonFriends;
    }
}