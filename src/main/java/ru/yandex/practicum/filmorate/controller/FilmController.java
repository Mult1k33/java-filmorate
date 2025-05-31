package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.utils.ControllersUtils.getNextId;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    public static final LocalDate DATE_FIRST_FILM = LocalDate.of(1895, 12, 28);
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение списка всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на добавление фильма {}", newFilm.getName());

        if (newFilm == null) {
            log.error("Попытка добавить null");
            throw new NullPointerException("Фильм не может быть null");
        }

        validateFilm(newFilm);
        newFilm.setId(getNextId(films.keySet()));
        films.put(newFilm.getId(), newFilm);

        log.info("Фильм {} успешно добавлен", newFilm.getName());
        return newFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма c id {}", film.getId());

        if (film.getId() == null) {
            log.error("Попытка обновить фильм с id = null");
            throw new ValidationException("Id фильма не может быть null");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Попытка обновить фильм, не найденный по id:{}", film.getId());
            throw new NotFoundException("Фильм с id:" + film.getId() + " не найден");
        }

        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм {} c id:{} успешно обновлен", film.getName(), film.getId());
        return film;
    }

    // Вспомогательный метод проверки выполнения необходимых условий
    private void validateFilm(Film film) {

        if (film.getName() == null || film.getName().isEmpty()) {
            log.error("Попытка добавить фильм без названия");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getReleaseDate().isBefore(DATE_FIRST_FILM)) {
            log.error("Попытка добавить фильм с недопустимой датой релиза");
            throw new ValidationException("Недопустимая дата релиза");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Попытка добавить фильм с недопустимой длиной описания");
            throw new ValidationException("Недопустимая длина описания");
        }

        if (film.getDuration() <= 0) {
            log.error("Попытка добавить фильм с некорректной продолжительностью");
            throw new ValidationException("Недопустимая продолжительность фильма");
        }
    }
}