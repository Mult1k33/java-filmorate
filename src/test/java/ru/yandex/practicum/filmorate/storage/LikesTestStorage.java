package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Likes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилитарный класс - тестовое хранилище Likes
 */

@Slf4j
@Repository
@Primary
public class LikesTestStorage implements LikesStorage {

    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeLikeFromFilm(Long filmId, Long userId) {
        filmLikes.getOrDefault(filmId, Set.of()).remove(userId);
    }

    @Override
    public Collection<Likes> getLikesOnFilm(Long filmId) {
        return filmLikes.getOrDefault(filmId, Set.of())
                .stream()
                .map(userId -> {
                    Likes like = new Likes();
                    like.setFilmId(filmId);
                    like.setUserId(userId);
                    return like;
                })
                .collect(Collectors.toList());
    }
}