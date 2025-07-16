package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {

    Collection<Genre> findAllGenres();

    Optional<Genre> findById(Long genreId);

    Collection<Genre> findGenreForFilm(Long filmId);

    void setGenreForFilm(Long filmId, Collection<Genre> genres);
}