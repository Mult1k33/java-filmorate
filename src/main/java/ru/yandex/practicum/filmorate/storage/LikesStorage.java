package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Likes;

import java.util.Collection;

public interface LikesStorage {

    void addLikeToFilm(Long filmId, Long userId);

    void removeLikeFromFilm(Long filmId, Long userId);

    Collection<Likes> getLikesOnFilm(Long filmId);
}