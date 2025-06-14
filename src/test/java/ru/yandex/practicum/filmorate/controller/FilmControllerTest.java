package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.utils.FilmValidate;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private FilmValidate filmValidate;


    @BeforeEach
    public void beforeEach() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmValidate = new FilmValidate();
        filmController = new FilmController(
                new FilmService(filmStorage, userStorage, filmValidate)
        );
    }

    // Тест успешного создания фильма с валидными данными
    @Test
    public void create_allRequiredFieldsValid_filmAddedWithGeneratedId() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1995, 2, 13));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId(), "Фильму не был присвоен id");
        assertEquals(1, filmController.findAll().size(),
                "Неверное количество фильмов после создания");
    }

    // Тест граничного условия для даты релиза - релиз раньше 28.12.1895 не допустим
    @Test
    public void create_releaseDateBeforeFirstFilm_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм с некорректной датой релиза");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при дате релиза раньше 28.12.1895");
    }

    // Тест граничного условия для даты релиза - релиз 28.12.1895 должен быть допустим
    @Test
    public void create_releaseDateEqualsFirstFilm_noValidationErrors() {
        Film film = new Film();
        film.setName("Старый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(15);

        assertDoesNotThrow(() -> filmController.create(film), "Дата релиза 28.12.1895 должна быть допустима");
    }

    // Тест добавления фильма без даты релиза - релиз не может быть null
    @Test
    public void create_releaseDateNull_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(null);
        film.setDuration(15);

        assertThrows(NullPointerException.class, () -> filmController.create(film),
                "Дата релиза не может быть null");
    }

    // Тест на обновление несуществующего фильма
    @Test
    public void update_nonExistentFilmId_throwsNotFoundException() {
        Film film = new Film();
        film.setId(14L);
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        assertThrows(NotFoundException.class, () -> filmController.update(film),
                "Ожидалось NotFoundException при обновлении несуществующего фильма");
    }

    // Тест проверяет валидацию Id при обновлении - Id не должен быть null
    @Test
    public void update_nullId_throwsValidationException() {
        Film film = new Film();
        film.setId(null);
        film.setName("Фильма с нулевым id");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 14));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.update(film),
                "Ожидалось ValidationException при обновлении с null ID");
    }

    // Тест на успешное обновление фильма
    @Test
    public void update_existingFilmWithValidData_fieldsUpdated() {
        // Создание фильма
        Film film = new Film();
        film.setName("Начало");
        film.setDescription("Сон внутри сна");
        film.setReleaseDate(LocalDate.of(2010, 7, 22));
        film.setDuration(148);
        Film createdFilm = filmController.create(film);

        // Обновление фильма
        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Уже не Начало");
        updatedFilm.setDescription("Просто описание");
        updatedFilm.setReleaseDate(LocalDate.of(1996, 1, 1));
        updatedFilm.setDuration(150);
        Film result = filmController.update(updatedFilm);

        assertEquals("Уже не Начало", result.getName(), "Название фильма не обновилось");
        assertEquals("Просто описание", result.getDescription(), "Описание фильма не обновилось");
        assertEquals(150, result.getDuration(), "Продолжительность фильма не обновилась");
    }

    // Тест на получение списка всех созданных фильмов
    @Test
    public void findAll_afterAddingTwoFilms_returnsCollectionSize2() {
        // Создаем 2 фильма
        Film film1 = new Film();
        film1.setName("Брат");
        film1.setDescription("Описание фильма");
        film1.setReleaseDate(LocalDate.of(1997, 12, 12));
        film1.setDuration(100);
        filmController.create(film1);

        Film film2 = new Film();
        film2.setName("Брат 2");
        film2.setDescription("Описание продолжения");
        film2.setReleaseDate(LocalDate.of(2000, 5, 11));
        film2.setDuration(127);
        filmController.create(film2);

        assertEquals(2, filmController.findAll().size(), "Неверное количество фильмов в списке");
    }

    // Тест граничного условия для описания - 200 символов должно быть допустимо
    @Test
    public void create_descriptionExactly200Chars_noValidationErrors() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 5, 30));
        film.setDuration(95);

        assertDoesNotThrow(() -> filmController.create(film), "Описание в 200 символов должно быть допустимо");
    }

    // Тест граничного условия описания - длина описания не может превышать 200 символов
    @Test
    public void create_description201Chars_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при описании длиной 201 символ ");
    }

    // Тест граничного условия для продолжительности: минимальная продолжительность (1 минута) должна быть допустима
    @Test
    public void create_durationOne_noValidationErrors() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 5, 30));
        film.setDuration(1);

        assertDoesNotThrow(() -> filmController.create(film),
                "Продолжительность фильма в 1 минуту должна быть допустима");
    }

    // Тест граничного условия для продолжительности - продолжительность должна быть больше 0
    @Test
    public void create_duration0_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при продолжительности 0");
    }

    // Тест граничного условия для продолжительности - продолжительность не может быть меньше 0
    @Test
    public void create_durationNegative_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при отрицательной продолжительности");
    }

    // Тест проверяет валидацию имени фильма - пустая строка недопустима
    @Test
    public void create_EmptyName_throwsValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 12, 31));
        film.setDuration(160);

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
        Film film = new Film();
        film.setName("Фильм для удаления");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);
        filmController.create(film);

        filmController.delete(film.getId());

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
        Film film = new Film();
        film.setName("Фильм для поиска");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);
        filmController.create(film);

        Film foundFilm = filmController.getById(film.getId());

        assertEquals(film, foundFilm, "Найденный фильм должен соответствовать созданному");
    }

    // Тест получения несуществующего фильма по Id
    @Test
    public void getById_nonExistentFilm_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> filmController.getById(999L),
                "Ожидалось NotFoundException при поиске несуществующего фильма");
    }

    // Тест добавления лайка фильму
    @Test
    public void addLike_validUserAndFilm_addsLike() {
        Film film = new Film();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);
        filmController.create(film);

        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userStorage.create(user);

        filmController.addLike(film.getId(), user.getId());

        Film updatedFilm = filmController.getById(film.getId());
        assertTrue(updatedFilm.getLikesByUsers().contains(user.getId()), "Лайк должен быть добавлен");
    }

    //Тест добавления лайка несуществующему фильму
    @Test
    public void addLike_nonExistentFilm_throwsNotFoundException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userStorage.create(user);

        assertThrows(NotFoundException.class, () -> filmController.addLike(999L, user.getId()),
                "Ожидалось NotFoundException при лайке несуществующего фильма");
    }

    // Тест добавления лайка от несуществующего пользователя
    @Test
    public void addLike_nonExistentUser_throwsNotFoundException() {
        Film film = new Film();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);
        filmController.create(film);

        assertThrows(NotFoundException.class, () -> filmController.addLike(film.getId(), 999L),
                "Ожидалось NotFoundException при лайке от несуществующего пользователя");
    }

    // Тест успешного удаления лайка
    @Test
    public void removeLike_existingLike_removesLike() {
        Film film = new Film();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);
        filmController.create(film);

        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userStorage.create(user);

        filmController.addLike(film.getId(), user.getId());
        filmController.removeLike(film.getId(), user.getId());

        Film updatedFilm = filmController.getById(film.getId());
        assertFalse(updatedFilm.getLikesByUsers().contains(user.getId()), "Лайк должен быть удален");
    }

    // Тест получения популярных фильмов
    @Test
    public void getPopularFilms_returnsFilmsOrderedByLikes() {
        Film film = new Film();
        film.setName("Фильм для 2 лайков");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 6, 14));
        film.setDuration(100);
        filmController.create(film);

        Film film2 = new Film();
        film2.setName("Фильм для 1 лайка");
        film2.setDescription("Описание");
        film2.setReleaseDate(LocalDate.of(2025, 6, 14));
        film2.setDuration(120);
        filmController.create(film2);

        Film film3 = new Film();
        film3.setName("Фильм без лайков");
        film3.setDescription("Описание");
        film3.setReleaseDate(LocalDate.of(2025, 5, 14));
        film3.setDuration(70);
        filmController.create(film3);

        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        userStorage.create(user);

        User user2 = new User();
        user2.setEmail("user@yandex.ru");
        user2.setLogin("user2_login");
        user2.setBirthday(LocalDate.of(1996, 2, 14));
        userStorage.create(user2);

        // film получает 2 лайка
        filmController.addLike(film.getId(), user.getId());
        filmController.addLike(film.getId(), user2.getId());

        // film2 получает 1 лайк
        filmController.addLike(film2.getId(), user2.getId());

        Collection<Film> popularFilms = filmController.getPopularFilms(2);

        assertEquals(2, popularFilms.size(), "Должны вернуться 2 фильма");
        assertEquals(film.getId(), popularFilms.iterator().next().getId(),
                "Первый фильм должен быть самым популярным");
    }
}