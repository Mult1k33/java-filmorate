package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.utils.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {
    private final Map<Long, Film> films = new HashMap<>();

    private FilmController filmController;
    private FilmTestStorage filmTestStorage;
    private UserTestStorage userTestStorage;
    private FilmValidate filmValidate;
    private GenreTestStorage genreTestStorage;
    private MpaTestStorage mpaTestStorage;
    private LikesTestStorage likesTestStorage;
    private UserController userController;
    private UserValidate userValidate;
    private FriendshipTestStorage friendshipTestStorage;

    @BeforeEach
    public void beforeEach() {
        films.clear();

        filmTestStorage = new FilmTestStorage();
        userTestStorage = new UserTestStorage();
        filmValidate = new FilmValidate();
        genreTestStorage = new GenreTestStorage();
        mpaTestStorage = new MpaTestStorage();
        likesTestStorage = new LikesTestStorage();

        filmController = new FilmController(
                new FilmService(filmTestStorage,
                        userTestStorage,
                        filmValidate,
                        genreTestStorage,
                        mpaTestStorage,
                        likesTestStorage)
        );

        userController = new UserController(new UserService(userTestStorage, userValidate, friendshipTestStorage));
    }

    // Тест успешного создания фильма с валидными данными
    @Test
    public void create_allRequiredFieldsValid_filmAddedWithGeneratedId() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1995, 2, 13));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId(), "Фильму не был присвоен id");
        assertEquals(1, filmTestStorage.findAll().size(),
                "Неверное количество фильмов после создания");
    }

    // Тест граничного условия для даты релиза - релиз раньше 28.12.1895 не допустим
    @Test
    public void create_releaseDateBeforeFirstFilm_throwsValidationException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм с некорректной датой релиза");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при дате релиза раньше 28.12.1895");
    }

    // Тест граничного условия для даты релиза - релиз 28.12.1895 должен быть допустим
    @Test
    public void create_releaseDateEqualsFirstFilm_noValidationErrors() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Старый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(15);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        assertDoesNotThrow(() -> filmController.create(film), "Дата релиза 28.12.1895 должна быть допустима");
    }

    // Тест добавления фильма без даты релиза - релиз не может быть null
    @Test
    public void create_releaseDateNull_throwsValidationException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(null);
        film.setDuration(15);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        assertThrows(NullPointerException.class, () -> filmController.create(film),
                "Дата релиза не может быть null");
    }

    // Тест на обновление несуществующего фильма
    @Test
    public void update_nonExistentFilmId_throwsNotFoundException() {
        UpdateFilmRequest film = new UpdateFilmRequest();
        film.setId(14L);
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        assertThrows(NotFoundException.class, () -> filmController.update(film),
                "Ожидалось NotFoundException при обновлении несуществующего фильма");
    }

    // Тест проверяет валидацию Id при обновлении - Id не должен быть null
    @Test
    public void update_nullId_throwsValidationException() {
        UpdateFilmRequest film = new UpdateFilmRequest();
        film.setId(null);
        film.setName("Фильма с нулевым id");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 14));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmController.update(film),
                "Ожидалось ValidationException при обновлении с null ID");
    }

    // Тест на успешное обновление фильма
    @Test
    public void update_existingFilmWithValidData_fieldsUpdated() {
        // Создание фильма
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Начало");
        film.setDescription("Сон внутри сна");
        film.setReleaseDate(LocalDate.of(2010, 7, 22));
        film.setDuration(148);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film);

        // Обновление фильма
        UpdateFilmRequest updatedFilm = new UpdateFilmRequest();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Уже не Начало");
        updatedFilm.setDescription("Просто описание");
        updatedFilm.setReleaseDate(LocalDate.of(1996, 1, 1));
        updatedFilm.setDuration(150);

        mpa.setId(2L);
        mpa.setName("PG");
        updatedFilm.setMpa(mpa);

        FilmDto result = filmController.update(updatedFilm);

        assertEquals("Уже не Начало", result.getName(), "Название фильма не обновилось");
        assertEquals("Просто описание", result.getDescription(), "Описание фильма не обновилось");
        assertEquals(150, result.getDuration(), "Продолжительность фильма не обновилась");
        assertEquals("PG", result.getMpa().getName(), "MPA-рейтинг фильма не обновился");
    }

    // Тест на получение списка всех созданных фильмов
    @Test
    public void findAll_afterAddingTwoFilms_returnsCollectionSize2() {
        // Создаем 2 фильма
        NewFilmRequest film1 = new NewFilmRequest();
        film1.setName("Брат");
        film1.setDescription("Описание фильма");
        film1.setReleaseDate(LocalDate.of(1997, 12, 12));
        film1.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");
        film1.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film1);

        NewFilmRequest film2 = new NewFilmRequest();
        film2.setName("Брат 2");
        film2.setDescription("Описание продолжения");
        film2.setReleaseDate(LocalDate.of(2000, 5, 11));
        film2.setDuration(127);

        Mpa mpa2 = new Mpa();
        mpa2.setId(1L);
        mpa2.setName("G");
        film2.setMpa(mpa2);

        FilmDto createdFilm2 = filmController.create(film2);

        assertEquals(2, filmController.findAll().size(), "Неверное количество фильмов в списке");
        assertEquals("Брат", createdFilm.getName(), "Название 1-го фильма должно быть Брат");
        assertEquals("Брат 2", createdFilm2.getName(), "Название 2-го фильма должно быть Брат 2");
    }

    // Тест граничного условия для описания - 200 символов должно быть допустимо
    @Test
    public void create_descriptionExactly200Chars_noValidationErrors() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 5, 30));
        film.setDuration(95);

        Mpa mpa = new Mpa();
        mpa.setId(3L);
        mpa.setName("PG-13");
        film.setMpa(mpa);

        assertDoesNotThrow(() -> filmController.create(film), "Описание в 200 символов должно быть допустимо");
    }

    // Тест граничного условия описания - длина описания не может превышать 200 символов
    @Test
    public void create_description201Chars_throwsValidationException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(4L);
        mpa.setName("R");
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при описании длиной 201 символ ");
    }

    // Тест граничного условия для продолжительности: минимальная продолжительность (1 минута) должна быть допустима
    @Test
    public void create_durationOne_noValidationErrors() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 5, 30));
        film.setDuration(1);

        Mpa mpa = new Mpa();
        mpa.setId(4L);
        mpa.setName("R");
        film.setMpa(mpa);

        assertDoesNotThrow(() -> filmController.create(film),
                "Продолжительность фильма в 1 минуту должна быть допустима");
    }

    // Тест граничного условия для продолжительности - продолжительность должна быть больше 0
    @Test
    public void create_duration0_throwsValidationException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(0);

        Mpa mpa = new Mpa();
        mpa.setId(4L);
        mpa.setName("R");
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при продолжительности 0");
    }

    // Тест граничного условия для продолжительности - продолжительность не может быть меньше 0
    @Test
    public void create_durationNegative_throwsValidationException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(-1);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при отрицательной продолжительности");
    }

    // Тест проверяет валидацию имени фильма - пустая строка недопустима
    @Test
    public void create_EmptyName_throwsValidationException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 12, 31));
        film.setDuration(160);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при пустом имени фильма");
    }

    // Тест проверяет невозможность добавить фильм равный null
    @Test
    public void create_nullFilm_throwsValidationException() {
        assertThrows(NullPointerException.class,
                () -> filmController.create(null),
                "Ожидалось NullPointerException, если объект Film = null");
    }

    // Тест успешного удаления фильма по Id
    @Test
    public void delete_existingFilm_removesFilm() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм для удаления");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        FilmDto filmForDelete = filmController.create(film);
        filmController.delete(filmForDelete.getId());

        assertEquals(0, filmController.findAll().size(), "Фильм должен быть удален");
    }

    // Тест удаления несуществующего фильма
    @Test
    public void delete_nonExistentFilm_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> filmController.delete(999L),
                "Ожидалось NotFoundException при удалении несуществующего фильма");
    }

    // Тест получения фильма по Id
    @Test
    public void getById_existingFilm_returnsFilm() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм для поиска");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film);
        FilmDto foundFilm = filmController.getById(createdFilm.getId());

        assertEquals(foundFilm, createdFilm, "Найденный фильм должен соответствовать созданному");
    }

    // Тест получения несуществующего фильма по Id
    @Test
    public void getById_nonExistentFilm_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> filmController.getById(999L),
                "Ожидалось NotFoundException при поиске несуществующего фильма");
    }

    // Тест добавления лайка
    @Test
    public void addLike_validUserAndFilm_addsLike() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film);

        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);
        filmController.addLike(createdFilm.getId(), createdUser.getId());

        FilmDto updatedFilm = filmController.getById(createdFilm.getId());
        assertTrue(updatedFilm.getLikesByUsers().contains(createdUser.getId()), "Лайк должен быть добавлен");
    }

    //Тест добавления лайка несуществующему фильму
    @Test
    public void addLike_nonExistentFilm_throwsNotFoundException() {
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);

        assertThrows(NotFoundException.class, () -> filmController.addLike(999L, createdUser.getId()),
                "Ожидалось NotFoundException при лайке несуществующего фильма");
    }

    // Тест добавления лайка от несуществующего пользователя
    @Test
    public void addLike_nonExistentUser_throwsNotFoundException() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film);

        assertThrows(NotFoundException.class, () -> filmController.addLike(createdFilm.getId(), 999L),
                "Ожидалось NotFoundException при лайке от несуществующего пользователя");
    }

    // Тест успешного удаления лайка
    @Test
    public void removeLike_existingLike_removesLike() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        FilmDto createdFilm = filmController.create(film);

        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        UserDto createdUser = userController.create(user);

        filmController.addLike(createdFilm.getId(), createdUser.getId());
        filmController.removeLike(createdFilm.getId(), createdFilm.getId());

        FilmDto updatedFilm = filmController.getById(createdFilm.getId());
        assertFalse(updatedFilm.getLikesByUsers().contains(createdUser.getId()), "Лайк должен быть удален");
    }

    // Тест получения популярных фильмов
    @Test
    public void getPopularFilms_returnsFilmsOrderedByLikes() {
        // Создаем 3 фильма
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Фильм для 2 лайков");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(5L);
        mpa.setName("NC-17");
        film.setMpa(mpa);

        FilmDto createdFilm1 = filmController.create(film);

        NewFilmRequest film2 = new NewFilmRequest();
        film2.setName("Фильм для 1 лайка");
        film2.setDescription("Описание");
        film2.setReleaseDate(LocalDate.of(2025, 6, 14));
        film2.setDuration(120);

        Mpa mpa2 = new Mpa();
        mpa2.setId(1L);
        mpa2.setName("G");
        film2.setMpa(mpa2);

        FilmDto createdFilm2 = filmController.create(film2);

        NewFilmRequest film3 = new NewFilmRequest();
        film3.setName("Фильм без лайков");
        film3.setDescription("Описание");
        film3.setReleaseDate(LocalDate.of(2025, 5, 14));
        film3.setDuration(70);

        Mpa mpa3 = new Mpa();
        mpa3.setId(4L);
        mpa3.setName("R");
        film3.setMpa(mpa3);

        FilmDto createdFilm3 = filmController.create(film3);

        // Создаем 2 пользователей
        NewUserRequest user = new NewUserRequest();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        UserDto createdUser = userController.create(user);

        NewUserRequest user2 = new NewUserRequest();
        user2.setEmail("user@yandex.ru");
        user2.setLogin("user2_login");
        user2.setBirthday(LocalDate.of(1996, 2, 14));
        UserDto createdUser2 = userController.create(user2);

        // createdFilm1 получает 2 лайка
        filmController.addLike(createdFilm1.getId(), createdUser.getId());
        filmController.addLike(createdFilm1.getId(), createdUser2.getId());

        // createdFilm2 получает 1 лайк
        filmController.addLike(createdFilm2.getId(), createdUser2.getId());

        Collection<FilmDto> popularFilms = filmController.getPopularFilms(2);

        assertEquals(2, popularFilms.size(), "Должны вернуться 2 фильма");
        assertEquals(createdFilm1.getId(), popularFilms.iterator().next().getId(),
                "Первый фильм должен быть самым популярным");
    }
}