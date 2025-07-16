package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        log.info("Получен запрос на получение списка всех фильмов");
        return filmService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@RequestBody NewFilmRequest newFilmRequest) {
        log.info("Получен запрос на добавление фильма {}", newFilmRequest.getName());
        return filmService.create(newFilmRequest);
    }

    @PutMapping
    public FilmDto update(@RequestBody UpdateFilmRequest filmRequest) {
        log.info("Получен запрос на обновление фильма c Id:{}", filmRequest.getId());
        return filmService.update(filmRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long filmId) {
        log.info("Получен запрос на удаление фильма с Id:{}", filmId);
        filmService.delete(filmId);
    }

    @GetMapping("/{id}")
    public FilmDto getById(@PathVariable("id") Long filmId) {
        log.info("Получен запрос на получение фильма с Id:{}", filmId);
        return filmService.findById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Получен запрос на добавление лайка фильму с Id:{} от пользователя с Id:{}", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Получен запрос на удаление лайка у фильма с Id:{} от пользователя с Id:{}", filmId, userId);
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение топ-{} фильмов по количеству лайков", count);
        return filmService.findPopularFilms(count);
    }
}