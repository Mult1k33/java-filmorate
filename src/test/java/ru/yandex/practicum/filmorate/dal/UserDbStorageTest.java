package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbStorageTest {

    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        // Очистка данных
        jdbcTemplate.execute("DELETE FROM users");
    }

    // Тест создания пользователя с валидными данными и получения его по id
    @Test
    public void createUser_withValidFields_return_userAddedWithGeneratedId() {
        User user = createUserTest("user@yandex.ru", "Mult1k", "Дмитрий",
                LocalDate.of(1995, 2, 13));

        // Сохранение пользователя в БД и поиск по id
        User newUser = userDbStorage.create(user);
        Optional<User> userOptional = userDbStorage.findById(newUser.getId());
        assertTrue(userOptional.isPresent(), "Пользователя должен существовать");

        // Получение пользователя из Optional
        User retrievedUser = userOptional.get();

        // Проверка основных полей пользователя
        assertUserEquals(user, retrievedUser);
    }

    // Тест получения списка всех созданных пользователей
    @Test
    public void findAll_afterAddingTwoUsers_returnsCollectionSize2() {
        User user = createUserTest("user@yandex.ru", "Mult1k", "Дмитрий",
                LocalDate.of(1995, 2, 13));
        User user2 = createUserTest("user@gmail.com", "Login2", "name",
                LocalDate.of(1995, 4, 24));

        // Сохранение пользователей в БД
        userDbStorage.create(user);
        userDbStorage.create(user2);

        Collection<User> users = userDbStorage.findAll();
        assertEquals(2, users.size(), "Должно быть найдено 2 пользователя");
    }

    // Тест обновления пользователя
    @Test
    public void update_existingUserWithValidData_fieldsUpdated() {
        User user = createUserTest("user@yandex.ru", "Mult1k", "Дмитрий",
                LocalDate.of(1995, 2, 13));

        // Сохранение пользователя в БД
        User createdUser = userDbStorage.create(user);

        // Обновление данных пользователя
        User updatedUser = createUserTest("user@gmail.com", "Login2", "name",
                LocalDate.of(1995, 4, 24));
        updatedUser.setId(createdUser.getId());

        // Обновление пользователя в БД и получение его из Optional
        userDbStorage.update(updatedUser);
        User retrievedUser = userDbStorage.findById(createdUser.getId()).orElseThrow();

        // Проверка основных полей пользователя
        assertEquals(updatedUser, retrievedUser);
    }

    // Тест удаления пользователя по id
    @Test
    public void delete_existingUser_removesUser() {
        User user = createUserTest("user@yandex.ru", "Mult1k", "Дмитрий",
                LocalDate.of(1995, 2, 13));

        // Сохранение пользователя в БД
        userDbStorage.create(user);

        // Удаление пользователя
        userDbStorage.delete(user.getId());
        Optional<User> userOptional = userDbStorage.findById(user.getId());
        assertEquals(Optional.empty(), userOptional, "Созданный пользователя должен быть удален");
    }

    // Вспомогательный метод для создания тестового пользователя
    private User createUserTest(
            String email,
            String login,
            String name,
            LocalDate birthday
    ) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    // Вспомогательный метод для проверки основных полей пользователя
    private void assertUserEquals(User expected, User actual) {
        assertEquals(expected.getEmail(), actual.getEmail(), "Email должен совпадать");
        assertEquals(expected.getLogin(), actual.getLogin(), "Логин пользователя должен совпадать");
        assertEquals(expected.getName(), actual.getName(), "Имя пользователя должно совпадать");
        assertEquals(expected.getBirthday(), actual.getBirthday(), "Дата рождения должна совпадать");
    }
}