package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.utils.FilmValidate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmValidate filmValidate;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        filmValidate.validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmValidate.validateFilm(film);
        return filmStorage.update(film);
    }

    public void delete(Long id) {
        filmStorage.delete(id);
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    // Метод добавления лайка фильму(по id) от пользователя c указанным id
    public void addLike(Long filmId, Long userId) {
        final Film film = filmStorage.getById(filmId);
        if (film == null) {
            log.warn("Попытка лайкнуть несуществующий фильм Id:{}", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        final User user = userStorage.getById(userId);
        if (user == null) {
            log.warn("Попытка лайка от несуществующего пользователя Id:{}", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        Set<Long> likes = film.getLikesByUsers();
        if (likes.contains(userId)) {
            log.debug("Пользователь с Id:{} уже ставил лайк фильму с Id:{}", userId, filmId);
            return;
        }

        likes.add(userId);
        film.setLikesByUsers(likes);
        filmStorage.update(film);
        log.debug("Пользователь с Id:{} поставил лайк фильму c Id:{}", userId, filmId);
    }

    // Метод удаления лайка у фильма(по id) от пользователя с указанным id
    public void removeLike(Long filmId, Long userId) {
        final Film film = filmStorage.getById(filmId);
        if (film == null) {
            log.warn("Попытка удалить лайк у несуществующего фильма с Id:{}", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        final User user = userStorage.getById(userId);
        if (user == null) {
            log.warn("Попытка удалить лайк от несуществующего пользователя c Id:{}", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        Set<Long> likes = film.getLikesByUsers();
        if (!likes.contains(userId)) {
            log.debug("Пользователь с Id:{} не ставил лайк фильму с Id:{}", userId, filmId);
            return;
        }
        likes.remove(userId);
        film.setLikesByUsers(likes);
        filmStorage.update(film);
        log.debug("Пользователь с Id:{} удалил лайк фильму c Id:{}", userId, filmId);
    }

    // Метод вывода 10 популярных фильмов по количеству лайков
    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным");
        }

        Collection<Film> films = filmStorage.findAll();
        return films.stream()
                .sorted((f1, f2) -> f2.getLikesByUsers().size() - f1.getLikesByUsers().size())
                .limit(count)
                .toList();
    }
}