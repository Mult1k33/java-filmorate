package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {

    private static final String FIND_ALL_GENRES_QUERY = "SELECT genre_id, name FROM genre";
    private static final String FIND_BY_ID_QUERY = "SELECT genre_id, name FROM genre WHERE genre_id = ?";
    private static final String FIND_GENRE_FOR_FILM_QUERY = "SELECT g.genre_id, g.name FROM genre As g " +
            "INNER JOIN film_genres AS fg ON g.genre_id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String INSERT_GENRES_FOR_FILM_QUERY = "INSERT INTO film_genres (film_id, genre_id) " +
            "VALUES(?, ?)";
    private static final String DELETE_ALL_GENRES_FOR_FILM_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    // Получение всех жанров
    @Override
    public Collection<Genre> findAllGenres() {
        return findMany(FIND_ALL_GENRES_QUERY);
    }

    // Получение жанра по id
    @Override
    public Optional<Genre> findById(Long genreId) {
        return findOne(FIND_BY_ID_QUERY, genreId);
    }

    // Получение жанров для указанного фильма
    @Override
    public Collection<Genre> findGenreForFilm(Long filmId) {
        return findMany(FIND_GENRE_FOR_FILM_QUERY, filmId);
    }

    // Добавление жанра для фильма
    @Override
    public void setGenreForFilm(Long filmId, Collection<Genre> genres) {
        if (genres == null) {
            return;
        }

        // Удаление всех текущих жанров
        jdbc.update(DELETE_ALL_GENRES_FOR_FILM_QUERY, filmId);

        // Добавление новых жанров
        for (Genre genre : genres) {
            jdbc.update(INSERT_GENRES_FOR_FILM_QUERY,
                    filmId, genre.getId());
        }
    }
}