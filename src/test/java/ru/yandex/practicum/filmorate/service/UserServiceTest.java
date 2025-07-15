package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipTestStorage;
import ru.yandex.practicum.filmorate.storage.UserTestStorage;
import ru.yandex.practicum.filmorate.utils.UserValidate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private final Map<Long, User> users = new HashMap<>();

    private UserController userController;
    private UserTestStorage userTestStorage;
    private UserValidate userValidate;
    private FriendshipTestStorage friendshipTestStorage;

    @BeforeEach
    public void beforeEach() {
        users.clear();

        userTestStorage = new UserTestStorage();
        userValidate = new UserValidate();
        friendshipTestStorage = new FriendshipTestStorage();

        userController = new UserController(new UserService(userTestStorage, userValidate, friendshipTestStorage));
    }

    // Тест успешного создания пользователя с валидными данными
    @Test
    public void create_allRequiredFieldsValid_userAddedWithGeneratedId() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);

        assertNotNull(createdUser.getId(), "Пользователю не был присвоен id");
        assertEquals(1, userController.findAll().size(),
                "Неверное количество пользователей после создания");
    }

    // Тест создания пользователя с пустым именем (должно подставляться значение login)
    @Test
    public void create_emptyName_nameEqualsLogin() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName("");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);

        assertEquals("user_login", createdUser.getName(),
                "При пустом имени должно подставляться значение login");
    }

    // Тест создания пользователя с null именем (должно подставляться значение login)
    @Test
    public void create_nullName_nameEqualsLogin() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName(null);
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);

        assertEquals("user_login", createdUser.getName(),
                "При null имени должно подставляться значение login");
    }

    // Тест валидации email - не может быть пустым
    @Test
    public void create_emptyEmail_throwsValidationException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при пустом email");
    }

    // Тест валидации email - должен быть корректным форматом
    @Test
    public void create_invalidEmailFormat_throwsValidationException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при некорректном email");
    }

    // Тест уникальности email - нельзя создать пользователя с существующим email
    @Test
    public void create_duplicateEmail_throwsDuplicateException() {
        NewUserRequest user1 = new NewUserRequest();
        user1.setEmail("user@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(user1);

        NewUserRequest user2 = new NewUserRequest();
        user2.setEmail("user@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(DuplicateException.class, () -> userController.create(user2),
                "Ожидалось DuplicateException при дублировании email");
    }

    // Тест добавления пользователя с email, равным null
    @Test
    public void create_nullEmail_throwsValidationException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(NullPointerException.class, () -> userController.create(user),
                "Ожидалось NullPointerException, если email = null");
    }

    // Тест валидации логина - не может быть пустым
    @Test
    public void create_emptyLogin_throwsValidationException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при пустом логине");
    }

    // Тест валидации логина - не может содержать пробелов
    @Test
    public void create_loginWithSpaces_throwsValidationException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при логине с пробелами");
    }

    // Тест валидации даты рождения - не может быть в будущем
    @Test
    public void create_birthdayInFuture_throwsValidationException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при дате рождения в будущем");
    }

    // Тест обновления несуществующего пользователя
    @Test
    public void update_nonExistentUserId_throwsNotFoundException() {
        UpdateUserRequest user = new UpdateUserRequest();
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
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        // Обновление пользователя
        UpdateUserRequest updatedUser = new UpdateUserRequest();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("new_login");
        updatedUser.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto result = userController.update(updatedUser);

        assertEquals("new@mail.ru", result.getEmail(), "Email не обновился");
        assertEquals("new_login", result.getLogin(), "Логин не обновился");
        assertEquals(LocalDate.of(1995, 2, 13), result.getBirthday(),
                "Дата рождения не обновилась");
    }

    // Тест получения списка всех пользователей
    @Test
    public void findAll_afterAddingTwoUsers_returnsCollectionSize2() {
        NewUserRequest user1 = new NewUserRequest();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user1);

        NewUserRequest user2 = new NewUserRequest();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto createdUser2 = userController.create(user2);

        assertEquals(2, userController.findAll().size(),
                "Неверное количество пользователей в списке");
        assertEquals("login1", createdUser.getLogin(),
                "Логин 1-го пользователя должен быть login1");
        assertEquals("login2", createdUser2.getLogin(),
                "Логин 2-го пользователя должен быть login2");
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
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto userForDelete = userController.create(user);
        userController.delete(userForDelete.getId());

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
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);
        UserDto foundUser = userController.getById(createdUser.getId());

        assertEquals(foundUser, createdUser, "Найденный пользователь должен соответствовать созданному");
    }

    // Тест получения несуществующего пользователя
    @Test
    public void getById_nonExistentUser_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userController.getById(999L),
                "Ожидалось NotFoundException при поиске несуществующего пользователя");
    }

    // Тест успешного добавления в друзья
    @Test
    public void addFriend_validUserAndFriend() {
        // Создаем основного пользователя
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        // Создаем друга
        NewUserRequest friend = new NewUserRequest();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto userForFriend = userController.create(friend);

        userController.addFriend(createdUser.getId(), userForFriend.getId());
        UserDto updatedUser = userController.getById(createdUser.getId());

        assertTrue(updatedUser.getFriends().contains(userForFriend.getId()),
                "Друг должен быть в списке пользователя");
        assertEquals(1, updatedUser.getFriends().size(),
                "У пользователя должен быть ровно один друг");
    }

    // Тест, что дружба является односторонней
    @Test
    public void addFriend_shouldCreateOneSidedFriendship() {
        // Создаем основного пользователя
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        // Создаем друга
        NewUserRequest friend = new NewUserRequest();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto userForFriend = userController.create(friend);

        userController.addFriend(createdUser.getId(), userForFriend.getId());
        UserDto updatedUser = userController.getById(createdUser.getId());
        UserDto updatedFriend = userController.getById(userForFriend.getId());

        // Проверка, что у пользователя появился друг
        assertTrue(updatedUser.getFriends().contains(userForFriend.getId()),
                "Друг должен быть в списке друзей пользователя");

        // Проверка, что у друга нет пользователя в друзьях
        assertFalse(updatedFriend.getFriends().contains(createdUser.getId()),
                "У друга не должно быть пользователя в друзьях");
        assertEquals(0, updatedFriend.getFriends().size(),
                "У друга не должно быть друзей");
    }

    // Тест на добавление несуществующего друга
    @Test
    public void addFriend_nonExistentFriend_throwsNotFounderException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        assertThrows(NotFoundException.class, () -> userController.addFriend(createdUser.getId(), 999L),
                "Ожидалось NotFoundException при добавлении в друзья несуществующего пользователя");
    }

    // Тест успешного удаления друга
    @Test
    public void deleteFriend_existingFriend_removesFriend() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        NewUserRequest friend = new NewUserRequest();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto friendForDelete = userController.create(friend);

        // добавляем друга и проверяем, что у пользователя появился друг
        userController.addFriend(createdUser.getId(), friendForDelete.getId());
        UserDto userWithFriend = userController.getById(createdUser.getId());

        assertTrue(userWithFriend.getFriends().contains(friendForDelete.getId()),
                "Друг должен быть в списке друзей пользователя");

        // удаляем друга и получаем обновленные данные
        userController.removeFriend(createdUser.getId(), friendForDelete.getId());
        UserDto userAfterRemove = userController.getById(createdUser.getId());

        // проверяем, что дружба удалилась у обоих
        assertFalse(userAfterRemove.getFriends().contains(friendForDelete.getId()),
                "Друг должен быть удален из списка пользователя");
        assertEquals(0, userAfterRemove.getFriends().size(),
                "Список друзей пользователя должен быть пустым");
    }

    // Тест на получение всех друзей
    @Test
    public void getFriends_returnsAllFriends() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        NewUserRequest friend = new NewUserRequest();
        friend.setEmail("user@yandex.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto userForFriend = userController.create(friend);

        NewUserRequest friend2 = new NewUserRequest();
        friend2.setEmail("friend@gmail.com");
        friend2.setLogin("Mult1k3");
        friend2.setBirthday(LocalDate.of(1988, 2, 28));
        UserDto userForFriend2 = userController.create(friend2);

        // добавляем пользователей в друзья
        userController.addFriend(createdUser.getId(), userForFriend.getId());
        userController.addFriend(createdUser.getId(), userForFriend2.getId());
        UserDto updatedUser = userController.getById(createdUser.getId());

        // Получаем список друзей
        Collection<UserDto> friends = userController.getFriends(updatedUser.getId());

        assertEquals(2, friends.size(), "Должно быть 2 друга");
        assertTrue(friends.stream().anyMatch(f -> f.getId().equals(userForFriend.getId())),
                "Друг 1 должен быть в списке");
        assertTrue(friends.stream().anyMatch(f -> f.getId().equals(userForFriend2.getId())),
                "Друг 2 должен быть в списке");
    }

    // Тест на получение пустого списка, если у пользователя нет друзей
    @Test
    public void getFriends_userWithNoFriends_returnsEmptyList() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        Collection<UserDto> friends = userController.getFriends(createdUser.getId());

        assertTrue(friends.isEmpty(), "Список друзей должен быть пустым");
    }

    // Тест на получение списка общих друзей у двух пользователей
    @Test
    public void getCommonFriends_returnsCommonFriends() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        NewUserRequest user2 = new NewUserRequest();
        user2.setEmail("user@yandex.ru");
        user2.setLogin("friend_login");
        user2.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto createdUser2 = userController.create(user2);

        NewUserRequest request = new NewUserRequest();
        request.setEmail("friend@gmail.com");
        request.setLogin("Mult1k3");
        request.setBirthday(LocalDate.of(1988, 2, 28));
        UserDto commonFriend = userController.create(request);

        // добавляем общего друга
        userController.addFriend(createdUser.getId(), commonFriend.getId());
        userController.addFriend(createdUser2.getId(), commonFriend.getId());

        // обновляем данные
        UserDto updatedUser = userController.getById(createdUser.getId());
        UserDto updatedUser2 = userController.getById(createdUser2.getId());

        // Проверяем общих друзей
        Collection<UserDto> commonFriends = userController.getCommonFriends(
                updatedUser.getId(),
                updatedUser2.getId()
        );

        assertEquals(1, commonFriends.size(), "Должен быть 1 общий друг");
        assertTrue(commonFriends.stream().anyMatch(f -> f.getId().equals(commonFriend.getId())),
                "Общий друг должен быть в списке");
    }
}