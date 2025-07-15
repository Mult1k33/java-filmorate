package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {

    private static final String FIND_ALL_MPA_QUERY = "SELECT rating_id, name FROM mpa_rating";
    private static final String FIND_BY_ID_QUERY = "SELECT rating_id, name FROM mpa_rating WHERE rating_id = ?";
    private static final String FIND_MPA_FILM_QUERY = "SELECT m.rating_id, m.name FROM films AS f " +
            "INNER JOIN mpa_rating AS m ON f.rating_id = m.rating_id " +
            "WHERE f.film_id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    // Получение всех рейтингов
    @Override
    public Collection<Mpa> findAll() {
        return findMany(FIND_ALL_MPA_QUERY);
    }

    // Получение рейтинга по его id
    @Override
    public Optional<Mpa> findMpaById(Long ratingId) {
        return findOne(FIND_BY_ID_QUERY, ratingId);
    }

    // Получение рейтинга для фильма
    @Override
    public Optional<Mpa> findMpaFilm(Long filmId) {
        return findOne(FIND_MPA_FILM_QUERY, filmId);
    }
}