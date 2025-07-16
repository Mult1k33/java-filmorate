package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        GenreDbStorage.class,
        LikesDbStorage.class,
        MpaDbStorage.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        LikesRowMapper.class,
        MpaRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final LikesDbStorage likesDbStorage;

    @BeforeEach
    public void beforeEach() {
        // Очистка данных
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM mpa_rating");
        jdbcTemplate.execute("DELETE FROM genre");

        // Инициализация mpa и жанров для тестов
        jdbcTemplate.update("INSERT INTO mpa_rating (rating_id, name) VALUES (1, 'G'), (2, 'PG')");
        jdbcTemplate.update("INSERT INTO genre (genre_id, name) VALUES (1, 'Комедия'), (2, 'Драма')");

        // Создание тестовых пользователей
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES " +
                "(1, 'user@mail.ru', 'login1', 'user_login', '1995-02-13'), " +
                "(2, 'user@yandex.ru', 'login2', 'user2_login', '1996-02-14')");
    }

    // Тест создания фильма с валидными данными и получения его по id
    @Test
    public void createFilm_withValidFields_return_filmAddedWithGeneratedId() {
        Film film = createTestFilm("Фильм", "Описание фильма",
                LocalDate.of(1995, 2, 13), 120, 1L, Set.of(1L));

        // Сохранение фильма в БД и поиск по id
        Film newFilm = filmDbStorage.create(film);
        Optional<Film> filmOptional = filmDbStorage.findById(newFilm.getId());
        assertTrue(filmOptional.isPresent(), "Фильм должен существовать");

        // Получение фильма из Optional
        Film retrievedFilm = filmOptional.get();

        // Проверка основных полей фильма
        assertFilmEquals(film, filmOptional.get());

        // Проверка связей фильма и MPA
        assertNotNull(retrievedFilm.getMpa(), "MPA не должен быть null");
        assertEquals(1L, retrievedFilm.getMpa().getId(), "id MPA должен совпадать");
        assertEquals("G", retrievedFilm.getMpa().getName(), "Название MPA должно быть 'G'");

        // Проверка связей фильма и жанров
        assertNotNull(retrievedFilm.getGenres(), "Жанры не должны быть null");
        assertEquals(1, retrievedFilm.getGenres().size(), "Должен быть 1 жанр");
        assertEquals(1L, retrievedFilm.getGenres().iterator().next().getId(),
                "id жанра должно быть 1");
    }

    // Тест получения списка всех созданных фильмов
    @Test
    public void findAll_afterAddingTwoFilms_returnsCollectionSize2() {
        Film film = createTestFilm("Фильм", "Описание фильма",
                LocalDate.of(1995, 2, 13), 120, 1L, Set.of(1L));
        Film film2 = createTestFilm("Фильм 2", "Описание фильма 2",
                LocalDate.of(2001, 3, 21), 145, 2L, Set.of(1L, 2L));

        // Сохранение фильмов в БД
        filmDbStorage.create(film);
        filmDbStorage.create(film2);

        Collection<Film> films = filmDbStorage.findAll();
        assertEquals(2, films.size(), "Должны быть найдены 2 фильма");
    }

    // Тест обновления фильма
    @Test
    public void update_existingFilmWithValidData_fieldsUpdated() {
        Film film = createTestFilm("Фильм", "Описание фильма",
                LocalDate.of(1995, 2, 13), 120, 1L, Collections.emptySet());

        // Сохранение фильма в БД
        Film createdFilm = filmDbStorage.create(film);

        // Обновление данных фильма
        Film updatedFilm = createTestFilm("Фильм 2", "Описание фильма 2",
                LocalDate.of(2001, 3, 21), 145, 2L, Set.of(1L, 2L));
        updatedFilm.setId(createdFilm.getId());

        //Обновление фильма в БД и получение его из Optional
        filmDbStorage.update(updatedFilm);
        Film retrievedFilm = filmDbStorage.findById(createdFilm.getId()).orElseThrow();

        // Проверка основных полей фильма
        assertFilmEquals(updatedFilm, retrievedFilm);

        // Проверка связей фильма и MPA
        assertNotNull(retrievedFilm.getMpa(), "MPA не должен быть null");
        assertEquals(2L, retrievedFilm.getMpa().getId(), "id MPA должен совпадать");
        assertEquals("PG", retrievedFilm.getMpa().getName(), "Название MPA должно быть 'PG'");

        // Проверка связей фильма и жанров
        assertNotNull(retrievedFilm.getGenres(), "Жанры не должны быть null");
        assertEquals(2, retrievedFilm.getGenres().size(), "Должно быть 2 жанра");
    }

    // Тест удаления фильма по id
    @Test
    public void delete_existingFilm_removesFilm() {
        Film film = createTestFilm("Фильм", "Описание фильма",
                LocalDate.of(1995, 2, 13), 120, 1L, Collections.emptySet());

        // Сохранение фильма в БД
        filmDbStorage.create(film);

        // Удаление фильма
        filmDbStorage.delete(film.getId());
        Optional<Film> filmOptional = filmDbStorage.findById(film.getId());
        assertEquals(Optional.empty(), filmOptional, "Созданный фильм должен быть удален");
    }

    // Тест на получение списка популярных фильмов
    @Test
    public void getPopularFilms_returnsFilmsOrderedByLikes() {
        Film film1 = createTestFilm("Фильм 1", "Описание 1",
                LocalDate.of(2000, 1, 1), 100, 1L, Set.of(1L));
        Film film2 = createTestFilm("Фильм 2", "Описание 2",
                LocalDate.of(2001, 1, 1), 120, 2L, Set.of(2L));

        // Сохранение фильмов в БД
        Film createdFilm = filmDbStorage.create(film1);
        Film createdFilm2 = filmDbStorage.create(film2);

        // Добавление лайков (film2 - 2 лайка, film1 - 0 лайков)
        likesDbStorage.addLikeToFilm(createdFilm2.getId(), 1L);
        likesDbStorage.addLikeToFilm(createdFilm2.getId(), 2L);
        likesDbStorage.addLikeToFilm(createdFilm.getId(), 2L);

        Collection<Film> popularFilms = filmDbStorage.findPopularFilms(2);

        assertNotNull(popularFilms, "Список не должен быть null");
        assertEquals(2, popularFilms.size(), "Должны вернуться 2 фильма");

        // Преобразование в список для проверки порядка
        List<Film> filmsList = new ArrayList<>(popularFilms);

        // Проверка порядка сортировки (по убыванию лайков)
        assertEquals(createdFilm2.getId(), filmsList.get(0).getId(),
                "Первым должен быть фильм с наибольшим количеством лайков");
        assertEquals(2, filmsList.get(0).getLikesByUsers().size(),
                "У первого фильма должно быть 2 лайка");

        assertEquals(createdFilm.getId(), filmsList.get(1).getId(),
                "Вторым должен быть фильм с меньшим количеством лайков");
        assertEquals(1, filmsList.get(1).getLikesByUsers().size(),
                "У второго фильма должен быть 1 лайк");
    }

    // Вспомогательный метод для создания тестового фильма
    private Film createTestFilm(
            String name,
            String description,
            LocalDate release_date,
            int duration,
            Long mpaId,
            Set<Long> genresId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(release_date);
        film.setDuration(duration);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);


        Set<Genre> genres = genresId.stream()
                .map(id -> {
                    Genre g = new Genre();
                    g.setId(id);
                    return g;
                })
                .collect(Collectors.toSet());
        film.setGenres(genres);

        return film;
    }

    // Вспомогательный метод для проверки основных полей фильма
    private void assertFilmEquals(Film expected, Film actual) {
        assertEquals(expected.getName(), actual.getName(), "Название фильма должно совпадать");
        assertEquals(expected.getDescription(), actual.getDescription(), "Описание должно совпадать");
        assertEquals(expected.getReleaseDate(), actual.getReleaseDate(), "Дата релиза должна совпадать");
        assertEquals(expected.getDuration(), actual.getDuration(),
                "Продолжительность фильма должна совпадать");
    }
}