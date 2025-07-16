package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Slf4j
@Service
public class GenreService {

    @Autowired
    private GenreStorage genreStorage;

    public Collection<Genre> findAll() {
        return genreStorage.findAllGenres();
    }

    public Genre findById(Long genreId) {
        return genreStorage.findById(genreId)
                .orElseThrow(() -> {
                    log.warn("Жанр с id:{} не найден", genreId);
                    return new NotFoundException("Жанр не найден");
                });
    }
}