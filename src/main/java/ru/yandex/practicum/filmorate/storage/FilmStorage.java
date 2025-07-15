package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film film);

    void delete(Long filmId);

    Film update(Film film);

    Optional<Film> findById(Long filmId);

    Collection<Film> findAll();

    Collection<Film> findPopularFilms(int count);
}