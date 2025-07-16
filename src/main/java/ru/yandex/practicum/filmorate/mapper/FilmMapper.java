package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    // Добавление фильма
    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(request.getMpa());
        film.setGenres(request.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        film.setLikesByUsers(new HashSet<>());
        return film;
    }

    // Изменение фильма
    public static Film mapToFilm(UpdateFilmRequest request) {
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(request.getMpa());
        film.setGenres(request.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        film.setLikesByUsers(new HashSet<>());
        return film;
    }

    // Преобразование в DTO
    public static FilmDto mapToDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setMpa(film.getMpa());

        dto.setGenres(film.getGenres() != null ?
                new LinkedHashSet<>(film.getGenres()) :
                new LinkedHashSet<>());

        dto.setLikesByUsers(film.getLikesByUsers() != null ?
                new HashSet<>(film.getLikesByUsers()) :
                new HashSet<>());

        return dto;
    }

    // Частичное изменение фильма
    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.getName() != null) {
            film.setName(request.getName());
        }
        if (request.getDescription() != null) {
            film.setDescription(request.getDescription());
        }
        if (request.getReleaseDate() != null) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.getDuration() != null) {
            film.setDuration(request.getDuration());
        }
        if (request.getMpa() != null) {
            film.setMpa(request.getMpa());
        }
        if (request.getGenres() != null) {
            film.setGenres(new LinkedHashSet<>(request.getGenres()));
        }
        return film;
    }
}