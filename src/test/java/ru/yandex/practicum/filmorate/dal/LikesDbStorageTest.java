package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.LikesRowMapper;
import ru.yandex.practicum.filmorate.model.Likes;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({LikesDbStorage.class, LikesRowMapper.class})
public class LikesDbStorageTest {

    private final LikesDbStorage likesDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");

        // Создание тестового фильма
        jdbcTemplate.update("INSERT INTO films (" +
                "film_id, " +
                "name, " +
                "description, " +
                "release_date, " +
                "duration, " +
                "rating_id) " +
                "VALUES (1, 'Тестовый фильм', 'Описание фильма', '2025-07-14', 120, 1)");

        // Создание тестовых пользователей
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES " +
                "(1, 'user@yandex.ru', 'Mult1k', 'Дмитрий', '1995-02-13'), " +
                "(2, 'user@gmail.com.ru', 'Friend', 'Саша', '1995-04-24')");
    }

    // Тест добавления лайка фильму
    @Test
    public void addLike_shouldAddLike() {
        likesDbStorage.addLikeToFilm(1L, 2L);
        Collection<Likes> likes = likesDbStorage.getLikesOnFilm(1L);
        assertEquals(1, likes.size(), "У тестового фильма должен быть 1 лайк");
    }

    // Тест удаления лайка у фильма
    @Test
    public void deleteLike_removesLikeFromFilm() {
        likesDbStorage.addLikeToFilm(1L, 2L);
        likesDbStorage.removeLikeFromFilm(1L, 2L);
        Collection<Likes> likes = likesDbStorage.getLikesOnFilm(1L);
        assertEquals(0, likes.size(), "У тестового фильма не должно быть лайков");
    }

    // Тест получения всех лайков у фильма
    @Test
    public void getAllLikesFromFilm_returnAllLikesFromFilm() {
        likesDbStorage.addLikeToFilm(1L, 1L);
        likesDbStorage.addLikeToFilm(1L, 2L);
        Collection<Likes> likes = likesDbStorage.getLikesOnFilm(1L);
        assertEquals(2, likes.size(), "У тестового фильма должно быть 2 лайка");
        assertTrue(likes.stream().anyMatch(like -> like.getUserId() == 1L),
                "Должен быть лайк от Дмитрия");
        assertTrue(likes.stream().anyMatch(like -> like.getUserId() == 2L),
                "Должен быть лайк от Саши");
    }
}