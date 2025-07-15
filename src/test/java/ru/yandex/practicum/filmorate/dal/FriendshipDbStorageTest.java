package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipDbStorage.class, FriendshipRowMapper.class, UserDbStorage.class, UserRowMapper.class})
public class FriendshipDbStorageTest {

    private final FriendshipDbStorage friendshipDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("DELETE FROM friendship");
        jdbcTemplate.execute("DELETE FROM users");

        // Создание тестовых пользователей
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES " +
                "(1, 'user@yandex.ru', 'Mult1k', 'Дмитрий', '1995-02-13'), " +
                "(2, 'user@gmail.com.ru', 'Friend', 'Саша', '1995-04-24'), " +
                "(3, 'user@yahoo.com', 'Friend2', 'Ксения', '1996-02-14')");
    }

    // Тест добавления в друзья и получение списка всех друзей
    @Test
    public void addFriend_addFriendship() {
        // Добавление пользователем user в друзья пользователей friend и friend2
        friendshipDbStorage.addFriend(1L, 2L);
        friendshipDbStorage.addFriend(1L, 3L);

        // Получение списка друзей пользователя user
        Collection<User> friends = friendshipDbStorage.findAllFriends(1L);
        assertEquals(2, friends.size(), "У пользователя должно быть 2 друга");

        // Получение списка друзей пользователей friend и friend2 - список должен быть пуст, тк дружба односторонняя
        Collection<User> friendFriends = friendshipDbStorage.findAllFriends(2L);
        assertEquals(0, friendFriends.size(), "У пользователя friend не должно быть друзей");
        Collection<User> friendFriends2 = friendshipDbStorage.findAllFriends(3L);
        assertEquals(0, friendFriends2.size(), "У пользователя friend2 не должно быть друзей");
    }

    // Тест удаления пользователя из друзей
    @Test
    public void delete_existingFriend_removesFriend() {
        // Добавление пользователем user в друзья пользователя friend
        friendshipDbStorage.addFriend(1L, 2L);

        // Удаление пользователем user из друзей пользователя friend
        friendshipDbStorage.removeFriend(1L, 2L);

        // Получение списка друзей пользователя user
        Collection<User> friends = friendshipDbStorage.findAllFriends(1L);
        assertTrue(friends.isEmpty(), "У пользователя не должно быть друзей");
    }

    // Тест получения общего списка друзей
    @Test
    public void getCommonFriends_returnsCommonFriends() {
        // Добавление пользователями с id 1L и 2L в друзья пользователя с id 3L
        friendshipDbStorage.addFriend(1L, 3L);
        friendshipDbStorage.addFriend(2L, 3L);

        // Получение списка общего друга(Friend2) у пользователей User и Friend
        Collection<User> commonFriends = friendshipDbStorage.findCommonFriends(1L, 2L);
        assertEquals(1, commonFriends.size(),
                "У пользователей Mult1k и Friend должен быть 1 общий друг");
        assertFalse(commonFriends.isEmpty(), "Список общих друзей пуст");
        assertEquals(3L, commonFriends.iterator().next().getId(), "Неверный id общего друга");
    }
}