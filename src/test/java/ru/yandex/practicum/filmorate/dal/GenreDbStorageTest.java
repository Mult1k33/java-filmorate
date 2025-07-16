package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
public class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        // Создание тестового фильма
        jdbcTemplate.update("INSERT INTO films (" +
                "film_id, " +
                "name, " +
                "description, " +
                "release_date, " +
                "duration, " +
                "rating_id) " +
                "VALUES (1, 'Тестовый фильм', 'Описание фильма', '2025-07-14', 120, 1)");
    }

    // Тест получения списка всех жанров из БД
    @Test
    public void findAllGenres_returnAllGenresFromDataSql() {
        Collection<Genre> genres = genreDbStorage.findAllGenres();
        assertEquals(6, genres.size(), "Должно быть 6 жанров в БД");
    }

    // Тест получения жанра из БД по id
    @Test
    public void findGenreById_returnGenreFromDataSql() {
        // Получение жанра из Optional
        Optional<Genre> genre = genreDbStorage.findById(1L);
        assertTrue(genre.isPresent(), "Жанр с id:1 должен существовать");
        assertEquals("Комедия", genre.get().getName(), "Жанр с id:1 должен быть комедией");
    }

    // Тест получения жанров для указанного фильма
    @Test
    public void findGenreForFilm_returnGenresForFilm() {
        // Добавляем жанры для тестового фильма с id:1
        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (1, 2)");

        Collection<Genre> genres = genreDbStorage.findGenreForFilm(1L);
        assertEquals(2, genres.size(), "У фильма должно быть 2 жанра");
        assertTrue(genres.stream().anyMatch(g -> g.getId() == 1L && g.getName().equals("Комедия")),
                "Должен быть жанр 'Комедия' (id=1)");
        assertTrue(genres.stream().anyMatch(g -> g.getId() == 2L && g.getName().equals("Драма")),
                "Должен быть жанр 'Драма' (id=2)");
    }

    // Тест добавления жанров для фильма
    @Test
    public void setGenresFilm_addGenresToFilm() {
        // Добавляем жанры для тестового фильма с id:1
        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (1, 2)");

        Collection<Genre> genres = genreDbStorage.findGenreForFilm(1L);
        assertTrue(genres.stream().anyMatch(g -> g.getId() == 1L && g.getName().equals("Комедия")),
                "Должен быть жанр 'Комедия' (id=1)");
        assertTrue(genres.stream().anyMatch(g -> g.getId() == 2L && g.getName().equals("Драма")),
                "Должен быть жанр 'Драма' (id=2)");
    }
}