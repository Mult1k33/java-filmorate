package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
public class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("DELETE FROM films");
        // Создание тестового фильма
        jdbcTemplate.update("INSERT INTO films (" +
                "film_id, " +
                "name, " +
                "description, " +
                "release_date, " +
                "duration, " +
                "rating_id) " +
                "VALUES (1, 'Тестовый фильм', 'Описание фильма', '2025-07-14', 120, 5)");
    }

    // Тест получения всех жанров
    @Test
    public void findAllMpa_returnAllMpaFromDataSql() {
        Collection<Mpa> allMpa = mpaDbStorage.findAll();
        assertEquals(5, allMpa.size(), "Должно быть 5 MPA-рейтингов в БД");
    }

    // Тест получения MPA-рейтинга из БД по id
    @Test
    public void findMpa_returnMpaFromDataSql() {
        // Получение MPA-рейтинга из Optional
        Optional<Mpa> mpa = mpaDbStorage.findMpaById(4L);
        assertTrue(mpa.isPresent(), "Жанр с id:4 должен существовать");
        assertEquals("R", mpa.get().getName(), "Жанр с id:4 должен быть R");
    }

    // Тест получения жанра для указанного фильма
    @Test
    public void findMpaForFilm_returnMpaToFilm() {
        Optional<Mpa> mpaFilm = mpaDbStorage.findMpaFilm(1L);
        assertTrue(mpaFilm.isPresent(), "Фильм должен иметь рейтинг");
        assertEquals(5, mpaFilm.get().getId(), "Фильм должен иметь рейтинг с id=5");
        assertEquals("NC-17", mpaFilm.get().getName(), "Фильм должен иметь рейтинг NC-17");
    }
}