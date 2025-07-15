package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    // Добавление пользователя
    public static User mapToUser(NewUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());
        user.setFriends(new HashSet<>());
        return user;
    }

    // Изменение пользователя
    public static User mapToUser(UpdateUserRequest request) {
        User user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setBirthday(request.getBirthday());
        user.setFriends(new HashSet<>());
        return user;
    }

    // Преобразование в DTO
    public static UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        dto.setFriends(user.getFriends() != null ?
                new HashSet<>(user.getFriends()) :
                new HashSet<>());
        return dto;
    }

    // Частичное изменение пользователя
    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getLogin() != null) {
            user.setLogin(request.getLogin());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        return user;
    }
}