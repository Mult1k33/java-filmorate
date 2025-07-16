package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    @Autowired
    private GenreStorage genreStorage;
    @Autowired
    private LikesStorage likesStorage;
    @Autowired
    private MpaStorage mpaStorage;

    private static final String INSERT_QUERY = "INSERT INTO films" +
            "(name, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE films SET " +
            "name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, " +
            "f.duration, m.rating_id, m.name AS mpa_name " +
            "FROM films AS f " +
            "INNER JOIN mpa_rating AS m ON f.rating_id = m.rating_id " +
            "WHERE f.film_id = ?";
    private static final String FIND_ALL_FILMS_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, " +
            "f.duration, m.rating_id, m.name AS mpa_name " +
            "FROM films AS f " +
            "INNER JOIN mpa_rating AS m ON f.rating_id = m.rating_id";
    private static final String FIND_POPULAR_FILMS_QUERY = "SELECT f.film_id, f.name, f.description, " +
            "f.release_date, f.duration, m.rating_id, m.name AS mpa_name, COUNT(l.user_id) AS likes " +
            "FROM films As f " +
            "INNER JOIN mpa_rating AS m ON f.rating_id = m.rating_id " +
            "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
            "GROUP BY f.film_id " +
            "ORDER BY likes DESC " +
            "LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    // Добавление фильма
    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);

        if (film.getGenres() != null) {
            genreStorage.setGenreForFilm(id, film.getGenres());
        }

        mpaStorage.findMpaFilm(id).ifPresent(film::setMpa);

        return film;
    }

    // Удаление фильма
    @Override
    public void delete(Long id) {
        delete(DELETE_QUERY, id);
    }

    // Изменение фильма
    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (film.getGenres() != null) {
            genreStorage.setGenreForFilm(film.getId(), film.getGenres());
        }

        // Возвращаем полный объект из БД
        return findById(film.getId()).orElseThrow();
    }

    // Получение фильма по id
    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, id);
        film.ifPresent(this::loadFilmData);
        return film;
    }

    // Получение всех фильмов
    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = findMany(FIND_ALL_FILMS_QUERY);
        films.forEach(this::loadFilmData);
        return films;
    }

    // Получение популярных фильмов по количеству лайков
    @Override
    public Collection<Film> findPopularFilms(int count) {
        Collection<Film> films = findMany(FIND_POPULAR_FILMS_QUERY, count);
        films.forEach(this::loadFilmData);
        return films;
    }

    // Общий вспомогательный метод для загрузки всех данных фильма
    private void loadFilmData(Film film) {
        loadLikes(film);
        loadGenres(film);
        loadMpa(film);
    }

    // Вспомогательный метод для загрузки данных о лайках фильма
    private void loadLikes(Film film) {
        Set<Long> likes = likesStorage.getLikesOnFilm(film.getId()).stream()
                .map(Likes::getUserId)
                .collect(Collectors.toCollection(HashSet::new));
        film.setLikesByUsers(likes);
    }

    // Вспомогательный метод для загрузки данных о жанрах фильма
    private void loadGenres(Film film) {
        Collection<Genre> genres = genreStorage.findGenreForFilm(film.getId());
        film.setGenres(genres != null ?
                genres.stream()
                        .sorted(Comparator.comparing(Genre::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new)) :
                new LinkedHashSet<>());
    }

    // Вспомогательный метод для загрузки данных о рейтинге фильма
    private void loadMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getName() == null) {
            mpaStorage.findMpaById(film.getMpa().getId())
                    .ifPresent(film::setMpa);
        }
    }
}