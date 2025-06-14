package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

import static ru.yandex.practicum.filmorate.utils.ControllersUtils.getNextId;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Set<String> filmNames = new HashSet<>();

    // Добавление фильма
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
        if (id == null || id <= 0) {
            throw new ValidationException("Идентификатор фильма должен быть определён и положительным");
        }

        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }

        filmNames.remove(films.get(id).getName().toLowerCase());
        films.remove(id);
    }

    // Изменение фильма
    @Override
    public Film update(Film film) {
        if (film.getId() == null || film.getId() <= 0) {
            throw new ValidationException("Идентификатор фильма должен быть определён и положительным");
        }

        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

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
    public Film getById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Идентификатор фильма должен быть определён и положительным");
        }

        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }

        return films.get(id);
    }

    // Получение всех фильмов
    @Override
    public Collection<Film> findAll() {
        return List.copyOf(films.values());
    }

    // Вспомогательный метод для проверки на наличие дубликата названия фильма
    private void checkFilmNamesUniqueness(String name) {
        if (filmNames.contains(name.toLowerCase())) {
            log.warn("Фильм '{}' уже существует", name);
            throw new DuplicateException("Фильм с таким названием уже существует");
        }
    }
}