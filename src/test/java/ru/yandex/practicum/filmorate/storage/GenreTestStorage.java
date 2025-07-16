package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилитарный класс - тестовое хранилище Genre
 */

@Slf4j
@Repository
@Primary
public class GenreTestStorage implements GenreStorage {
    private final Map<Long, Genre> genres = new HashMap<Long, Genre>() {{
        Genre genre1 = new Genre();
        genre1.setId(1L);
        genre1.setName("Комедия");
        put(1L, genre1);

        Genre genre2 = new Genre();
        genre2.setId(2L);
        genre2.setName("Драма");
        put(2L, genre2);
    }};

    // Хранение связей жанров и фильма
    private final Map<Long, Set<Long>> filmToGenres = new HashMap<>();

    @Override
    public Collection<Genre> findAllGenres() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public Collection<Genre> findGenreForFilm(Long filmId) {
        return filmToGenres.getOrDefault(filmId, Set.of())
                .stream()
                .map(genres::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void setGenreForFilm(Long filmId, Collection<Genre> genres) {
        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        filmToGenres.put(filmId, genreIds);
    }
}