package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Likes;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.util.Collection;

@Slf4j
@Repository
public class LikesDbStorage extends BaseDbStorage<Likes> implements LikesStorage {

    private static final String INSERT_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_ON_FILM_QUERY = "SELECT film_id, user_id FROM likes WHERE film_id = ?";

    public LikesDbStorage(JdbcTemplate jdbc, RowMapper<Likes> mapper) {
        super(jdbc, mapper);
    }

    // Добавление лайка фильму
    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        update(INSERT_QUERY, filmId, userId);
    }

    // Удаление лайка у фильма
    @Override
    public void removeLikeFromFilm(Long filmId, Long userId) {
        update(DELETE_QUERY, filmId, userId);
    }

    // Получение всех лайков фильма
    @Override
    public Collection<Likes> getLikesOnFilm(Long filmId) {
        return findMany(FIND_LIKES_ON_FILM_QUERY, filmId);
    }
}