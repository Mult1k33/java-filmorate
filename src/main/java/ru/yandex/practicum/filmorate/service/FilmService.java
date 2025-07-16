package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.utils.FilmValidate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmValidate filmValidate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final LikesStorage likesStorage;

    // Получение всех фильмов
    public Collection<FilmDto> findAll() {
        return filmStorage.findAll().stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    // Получение фильма по id
    public FilmDto findById(Long filmId) {
        return filmStorage.findById(filmId)
                .map(FilmMapper::mapToDto)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    // Добавление фильма
    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        filmValidate.validateFilm(film);

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                genreStorage.findById(genre.getId())
                        .orElseThrow(() -> {
                            log.warn("Жанр с id:{} не найден", genre.getId());
                            return new NotFoundException("Жанр не найден");
                        });
            });
        }

        Long mpaId = film.getMpa().getId();
        if (mpaId != null) {
            mpaStorage.findMpaById(mpaId)
                    .orElseThrow(() -> {
                        log.warn("Рейтинг с id:{} не найден", mpaId);
                        return new NotFoundException("Рейтинг MPA не найден");
                    });
        }

        film = filmStorage.create(film);
        return FilmMapper.mapToDto(film);
    }

    // Обновление фильма
    public FilmDto update(UpdateFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        filmValidate.validateFilm(film);

        Film oldFilm = filmStorage.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Фильм с id:{} не найден", request.getId());
                    return new NotFoundException("Фильм не найден");
                });

        Long mpaId = film.getMpa().getId();
        if (mpaId != null) {
            mpaStorage.findMpaById(mpaId)
                    .orElseThrow(() -> {
                        log.warn("Рейтинг с id:{} не найден", mpaId);
                        return new NotFoundException("Рейтинг MPA не найден");
                    });
        }

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                genreStorage.findById(genre.getId())
                        .orElseThrow(() -> {
                            log.warn("Жанр с id:{} не найден", genre.getId());
                            return new NotFoundException("Жанр не найден");
                        });
            });
        }

        film = FilmMapper.updateFilmFields(oldFilm, request);
        film.setLikesByUsers(oldFilm.getLikesByUsers());
        return FilmMapper.mapToDto(filmStorage.update(film));
    }

    // Удаление фильма по id
    public void delete(Long filmId) {
        filmStorage.delete(filmId);
    }

    // Метод добавления лайка фильму(по id) от пользователя c указанным id
    public void addLike(Long filmId, Long userId) {
        final Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Попытка лайкнуть несуществующий фильм Id:{}", filmId);
                    throw new NotFoundException("Фильм не найден");
                });

        final User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка лайка от несуществующего пользователя Id:{}", userId);
                    throw new NotFoundException("Пользователь не найден");
                });

        Set<Long> likes = film.getLikesByUsers();
        if (likes.contains(userId)) {
            log.debug("Пользователь с Id:{} уже ставил лайк фильму с Id:{}", userId, filmId);
            return;
        }
        likes.add(userId);
        film.setLikesByUsers(likes);
        likesStorage.addLikeToFilm(filmId, userId);
        log.debug("Пользователь с Id:{} поставил лайк фильму c Id:{}", userId, filmId);
    }

    // Метод удаления лайка у фильма(по id) от пользователя с указанным id
    public void removeLike(Long filmId, Long userId) {
        final Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Попытка удалить лайк у несуществующего фильма с Id:{}", filmId);
                    throw new NotFoundException("Фильм не найден");
                });

        final User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка удалить лайк от несуществующего пользователя c Id:{}", userId);
                    throw new NotFoundException("Пользователь не найден");
                });

        Set<Long> likes = film.getLikesByUsers();
        if (!likes.contains(userId)) {
            log.debug("Пользователь с Id:{} не ставил лайк фильму с Id:{}", userId, filmId);
            return;
        }
        likes.remove(userId);
        film.setLikesByUsers(likes);
        likesStorage.removeLikeFromFilm(filmId, userId);
        log.debug("Пользователь с Id:{} удалил лайк фильму c Id:{}", userId, filmId);
    }

    // Метод получения популярных фильмов по количеству лайков
    public Collection<FilmDto> findPopularFilms(int count) {
        Collection<Film> films = filmStorage.findAll();
        return films.stream()
                .sorted((f1, f2) -> f2.getLikesByUsers().size() - f1.getLikesByUsers().size())
                .limit(count)
                .map(FilmMapper::mapToDto)
                .toList();
    }
}