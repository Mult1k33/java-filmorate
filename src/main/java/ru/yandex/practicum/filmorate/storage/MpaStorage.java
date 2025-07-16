package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {

    Collection<Mpa> findAll();

    Optional<Mpa> findMpaById(Long ratingId);

    Optional<Mpa> findMpaFilm(Long filmId);
}