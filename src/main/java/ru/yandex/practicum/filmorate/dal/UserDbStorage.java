package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) " +
            "VALUES(?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT user_id, email, login, name, birthday " +
            "FROM users " +
            "WHERE user_id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT user_id, email, login, name, birthday FROM users";
    private static final String FIND_FRIENDS_QUERY = "SELECT user_id, email, login, name, birthday FROM users " +
            "WHERE user_id IN(SELECT friend_id FROM friendship WHERE user_id = ?)";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    // Добавление пользователя
    @Override
    public User create(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    // Удаление пользователя
    @Override
    public void delete(Long id) {
        delete(DELETE_QUERY, id);
    }

    // Изменение пользователя
    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    // Получение пользователя по id
    @Override
    public Optional<User> findById(Long id) {
        Optional<User> user = findOne(FIND_BY_ID_QUERY, id);
        user.ifPresent(value -> value.setFriends(
                findMany(FIND_FRIENDS_QUERY, value.getId())
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toSet())
        ));
        return user;
    }

    // Получение всех пользователей
    @Override
    public Collection<User> findAll() {
        Collection<User> users = findMany(FIND_ALL_USERS_QUERY);

        if (users.isEmpty()) {
            return users;
        }

        users.forEach(user -> {
            user.setFriends(
                    findMany(FIND_FRIENDS_QUERY, user.getId())
                            .stream()
                            .map(User::getId)
                            .collect(Collectors.toSet())
            );
        });

        return users;
    }
}