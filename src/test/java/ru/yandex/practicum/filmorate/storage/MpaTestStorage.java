package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

/**
 * Утилитарный класс - тестовое хранилище Mpa
 */

@Slf4j
@Repository
@Primary
public class MpaTestStorage implements MpaStorage {

    // Добавление MPA-рейтингов в тестовое хранилище Mpa
    private final Map<Long, Mpa> mpas = new HashMap<Long, Mpa>() {{
        put(1L, createMpa(1L, "G"));
        put(2L, createMpa(2L, "PG"));
        put(3L, createMpa(3L, "PG-13"));
        put(4L, createMpa(4L, "R"));
        put(5L, createMpa(5L, "NC-17"));
    }};

    // Хранение связей фильмов и MPA
    private final Map<Long, Long> filmToMpa = new HashMap<>();

    @Override
    public Collection<Mpa> findAll() {
        return mpas.values();
    }

    @Override
    public Optional<Mpa> findMpaById(Long id) {
        return Optional.ofNullable(mpas.get(id));
    }

    @Override
    public Optional<Mpa> findMpaFilm(Long filmId) {
        return Optional.ofNullable(filmToMpa.get(filmId))
                .flatMap(this::findMpaById);
    }

    // Вспомогательный метод для добавления рейтингов
    private Mpa createMpa(Long id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }
}