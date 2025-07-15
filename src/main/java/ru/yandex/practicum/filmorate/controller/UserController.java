package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody NewUserRequest newUserRequest) {
        log.info("Получен запрос на добавление нового пользователя {}", newUserRequest.getLogin());
        return userService.create(newUserRequest);
    }

    @PutMapping
    public UserDto update(@RequestBody UpdateUserRequest userRequest) {
        log.info("Получен запрос на обновление пользователя с id:{}", userRequest.getId());
        return userService.update(userRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long userId) {
        log.info("Получен запрос на удаление пользователя с Id:{}", userId);
        userService.delete(userId);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable("id") Long userId) {
        log.info("Получен запрос на получение пользователя с Id:{}", userId);
        return userService.getById(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Получен запрос на добавление в друзья к пользователю с Id:{} пользователя:{}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Получен запрос на удаление из друзей пользователя с Id:{} пользователя:{}", userId, friendId);
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getFriends(@PathVariable("id") Long userId) {
        log.info("Получен запрос на получение списка всех друзей пользователя с Id:{}", userId);
        return userService.findAllFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> getCommonFriends(@PathVariable("id") Long userId, @PathVariable("otherId") Long otherId) {
        log.info("Получен запрос на получение списка общих друзей у пользователей с Id:{} и {}", userId, otherId);
        return userService.findCommonFriends(userId, otherId);
    }
}