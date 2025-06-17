package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.utils.UserValidate;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private InMemoryUserStorage userStorage;
    private UserValidate userValidate;

    @BeforeEach
    public void beforeEach() {
        userStorage = new InMemoryUserStorage();
        userValidate = new UserValidate();
        userController = new UserController(
                new UserService(userStorage, userValidate)
        );
    }

    // Тест успешного создания пользователя с валидными данными
    @Test
    public void create_allRequiredFieldsValid_userAddedWithGeneratedId() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId(), "Пользователю не был присвоен id");
        assertEquals(1, userController.findAll().size(),
                "Неверное количество пользователей после создания");
    }

    // Тест создания пользователя с пустым именем (должно подставляться значение login)
    @Test
    public void create_emptyName_nameEqualsLogin() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName("");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        User createdUser = userController.create(user);

        assertEquals("user_login", createdUser.getName(),
                "При пустом имени должно подставляться значение login");
    }

    // Тест создания пользователя с null именем (должно подставляться значение login)
    @Test
    public void create_nullName_nameEqualsLogin() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName(null);
        user.setBirthday(LocalDate.of(1995, 2, 13));

        User createdUser = userController.create(user);

        assertEquals("user_login", createdUser.getName(),
                "При null имени должно подставляться значение login");
    }

    // Тест валидации email - не может быть пустым
    @Test
    public void create_emptyEmail_throwsValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при пустом email");
    }

    // Тест валидации email - должен быть корректным форматом
    @Test
    public void create_invalidEmailFormat_throwsValidationException() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при некорректном email");
    }

    // Тест уникальности email - нельзя создать пользователя с существующим email
    @Test
    public void create_duplicateEmail_throwsDuplicateException() {
        User user1 = new User();
        user1.setEmail("user@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(user1);

        User user2 = new User();
        user2.setEmail("user@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(DuplicateException.class, () -> userController.create(user2),
                "Ожидалось DuplicateException при дублировании email");
    }

    // Тест добавления пользователя с email, равным null
    @Test
    public void create_nullEmail_throwsValidationException() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(NullPointerException.class, () -> userController.create(user),
                "Ожидалось NullPointerException, если email = null");
    }

    // Тест валидации логина - не может быть пустым
    @Test
    public void create_emptyLogin_throwsValidationException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при пустом логине");
    }

    // Тест валидации логина - не может содержать пробелов
    @Test
    public void create_loginWithSpaces_throwsValidationException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при логине с пробелами");
    }

    // Тест валидации даты рождения - не может быть в будущем
    @Test
    public void create_birthdayInFuture_throwsValidationException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при дате рождения в будущем");
    }

    // Тест обновления несуществующего пользователя
    @Test
    public void update_nonExistentUserId_throwsNotFoundException() {
        User user = new User();
        user.setId(999L);
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(NotFoundException.class, () -> userController.update(user),
                "Ожидалось NotFoundException при обновлении несуществующего пользователя");
    }

    // Тест успешного обновления пользователя
    @Test
    public void update_existingUserWithValidData_fieldsUpdated() {
        // Создание пользователя
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        User createdUser = userController.create(user);

        // Обновление пользователя
        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("new_login");
        updatedUser.setBirthday(LocalDate.of(1995, 2, 13));
        User result = userController.update(updatedUser);

        assertEquals("new@mail.ru", result.getEmail(), "Email не обновился");
        assertEquals("new_login", result.getLogin(), "Логин не обновился");
        assertEquals(LocalDate.of(1995, 2, 13), result.getBirthday(),
                "Дата рождения не обновилась");
    }

    // Тест получения списка всех пользователей
    @Test
    public void findAll_afterAddingTwoUsers_returnsCollectionSize2() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user1);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(user2);

        assertEquals(2, userController.findAll().size(),
                "Неверное количество пользователей в списке");
    }

    // Тест проверяет невозможность добавить пользователя равного null
    @Test
    public void create_nullUser_throwsValidationException() {
        assertThrows(NullPointerException.class,
                () -> userController.create(null),
                "Ожидалось NullPointerException при null пользователе");
    }

    // Тест успешного удаления пользователя
    @Test
    public void delete_existingUser_removesUser() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        userController.delete(user.getId());
        assertEquals(0, userController.findAll().size(), "Пользователь должен быть удален");
    }

    // Тест удаления несуществующего пользователя
    @Test
    public void delete_nonExistentUser_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userController.delete(999L),
                "Ожидалось NotFoundException при удалении несуществующего пользователя");
    }

    // Тест получения пользователя по Id
    @Test
    public void getById_existingUser_returnsFilm() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        User foundUser = userController.getById(user.getId());

        assertEquals(user, foundUser, "Найденный пользователь должен соответствовать созданному");
    }

    // Тест получения несуществующего пользователя
    @Test
    public void getById_nonExistentUser_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userController.getById(999L),
                "Ожидалось NotFoundException при поиске несуществующего пользователя");
    }

    // Тест добавления в друзья
    @Test
    public void addFriend_validUserAndFriend() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        User friend = new User();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(friend);

        userController.addFriend(user.getId(), friend.getId());

        User updatedUser = userController.getById(user.getId());
        User updatedFriend = userController.getById(friend.getId());
        assertAll(
                () -> assertTrue(updatedUser.getFriends().contains(friend.getId()),
                        "Друг должен быть в списке пользователя"),
                () -> assertTrue(updatedFriend.getFriends().contains(user.getId()),
                        "Пользователь должен быть в списке друга"),
                () -> assertEquals(1, updatedUser.getFriends().size(),
                        "Должен быть ровно один друг"),
                () -> assertEquals(1, updatedFriend.getFriends().size(),
                        "Должен быть ровно один друг")
        );
    }

    // Тест на добавление несуществующего друга
    @Test
    public void addFriend_nonExistentFriend_throwsNotFounderException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        assertThrows(NotFoundException.class, () -> userController.addFriend(user.getId(), 999L),
                "Ожидалось NotFoundException при добавлении в друзья несуществующего пользователя");
    }

    // Тест успешного удаления друга
    @Test
    public void deleteFriend_existingFriend_removesFriend() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        User friend = new User();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(friend);

        // добавляем друга и проверяем, что дружба взаимна у двух пользователей
        userController.addFriend(user.getId(), friend.getId());
        User updatedUser = userController.getById(user.getId());
        User updatedFriend = userController.getById(friend.getId());

        assertAll(
                () -> assertTrue(updatedUser.getFriends().contains(friend.getId()),
                        "Друг должен быть в списке пользователя"),
                () -> assertTrue(updatedFriend.getFriends().contains(user.getId()),
                        "Пользователь должен быть в списке друга"),
                () -> assertEquals(1, updatedUser.getFriends().size(),
                        "Должен быть ровно один друг"),
                () -> assertEquals(1, updatedFriend.getFriends().size(),
                        "Должен быть ровно один друг")
        );

        // удаляем друга
        userController.removeFriend(updatedUser.getId(), updatedFriend.getId());
        // Получаем обновленные данные
        User userAfterRemove = userController.getById(user.getId());
        User friendAfterRemove = userController.getById(friend.getId());

        // проверяем, что дружба удалилась у обоих
        assertAll(
                () -> assertFalse(userAfterRemove.getFriends().contains(friend.getId()),
                        "Друг должен быть удален из списка пользователя"),
                () -> assertFalse(friendAfterRemove.getFriends().contains(user.getId()),
                        "Пользователь должен быть удален из списка друга"),
                () -> assertEquals(0, userAfterRemove.getFriends().size(),
                        "Список друзей пользователя должен быть пустым"),
                () -> assertEquals(0, friendAfterRemove.getFriends().size(),
                        "Список друзей друга должен быть пустым")
        );
    }

    // Тест на получение всех друзей
    @Test
    public void getFriends_returnsAllFriends() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        User friend = new User();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(friend);

        User friend2 = new User();
        friend2.setEmail("friend@gmail.com");
        friend2.setLogin("Mult1k3");
        friend2.setBirthday(LocalDate.of(1988, 2, 28));
        userController.create(friend2);

        // добавляем пользователей в друзья
        userController.addFriend(user.getId(), friend.getId());
        userController.addFriend(user.getId(), friend2.getId());
        User updatedUser = userController.getById(user.getId());

        // Получаем список друзей
        Collection<User> friends = userController.getFriends(updatedUser.getId());

        assertEquals(2, friends.size(), "Должно быть 2 друга");
        assertTrue(friends.stream().anyMatch(f -> f.getId().equals(friend.getId())),
                "Друг 1 должен быть в списке");
        assertTrue(friends.stream().anyMatch(f -> f.getId().equals(friend2.getId())),
                "Друг 2 должен быть в списке");
    }

    // Тест на получение пустого списка, если у пользователя нет друзей
    @Test
    public void getFriends_userWithNoFriends_returnsEmptyList() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        Collection<User> friends = userController.getFriends(user.getId());

        assertTrue(friends.isEmpty(), "Список друзей должен быть пустым");
    }

    // Тест на получение списка общих друзей у двух пользователей
    @Test
    public void getCommonFriends_returnsCommonFriends() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userController.create(user);

        User user2 = new User();
        user2.setEmail("user@yandex.ru");
        user2.setLogin("friend_login");
        user2.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("friend@gmail.com");
        commonFriend.setLogin("Mult1k3");
        commonFriend.setBirthday(LocalDate.of(1988, 2, 28));
        userController.create(commonFriend);

        // добавляем общего друга
        userController.addFriend(user.getId(), commonFriend.getId());
        userController.addFriend(user2.getId(), commonFriend.getId());

        // обновляем данные
        User updatedUser = userController.getById(user.getId());
        User updatedUser2 = userController.getById(user2.getId());

        // Проверяем общих друзей
        Collection<User> commonFriends = userController.getCommonFriends(
                updatedUser.getId(),
                updatedUser2.getId()
        );

        assertEquals(1, commonFriends.size(), "Должен быть 1 общий друг");
        assertTrue(commonFriends.stream().anyMatch(f -> f.getId().equals(commonFriend.getId())),
                "Общий друг должен быть в списке");
    }
}