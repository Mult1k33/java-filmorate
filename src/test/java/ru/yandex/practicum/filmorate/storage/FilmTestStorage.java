package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.utils.ControllersUtils.getNextId;

/**
 * Утилитарный класс - тестовое хранилище для Film
 */

@Slf4j
@Repository
@Primary
public class FilmTestStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Set<String> filmNames = new HashSet<>();

    // Создание фильма
    @Override
    public Film create(Film film) {
        checkFilmNamesUniqueness(film.getName());
        filmNames.add(film.getName().toLowerCase());
        film.setId(getNextId(films.keySet()));
        film.setLikesByUsers(new HashSet<>());
        films.put(film.getId(), film);
        return film;
    }

    // Удаление фильма по id
    @Override
    public void delete(Long id) {
        validateFilmId(id);
        filmNames.remove(films.get(id).getName().toLowerCase());
        films.remove(id);
    }

    // Обновление фильма
    @Override
    public Film update(Film film) {
        validateFilmId(film.getId());

        final Film oldFilm = films.get(film.getId());

        if (!oldFilm.getName().equalsIgnoreCase(film.getName())) {
            filmNames.remove(oldFilm.getName().toLowerCase());
            filmNames.add(film.getName().toLowerCase());
        }

        if (film.getLikesByUsers() == null) {
            film.setLikesByUsers(new HashSet<>());
        }

        films.put(film.getId(), film);
        return film;
    }

    // Получение фильма по id
    @Override
    public Optional<Film> findById(Long id) {
        validateFilmId(id);
        return Optional.ofNullable(films.get(id));
    }

    // Получение всех фильмов
    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    // Получения списка популярных фильмов
    @Override
    public Collection<Film> findPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikesByUsers().size(),
                        f1.getLikesByUsers().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    // Вспомогательный метод для проверки на наличие дубликата названия фильма
    private void checkFilmNamesUniqueness(String name) {
        if (filmNames.contains(name.toLowerCase())) {
            log.warn("Фильм '{}' уже существует", name);
            throw new DuplicateException("Фильм с таким названием уже существует");
        }
    }

    // Вспомогательный метод для валидации фильма
    private void validateFilmId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Идентификатор фильма должен быть определён и положительным");
        }

        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }
}