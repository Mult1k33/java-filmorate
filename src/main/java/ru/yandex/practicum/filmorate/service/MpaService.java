package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
public class MpaService {

    @Autowired
    private MpaStorage mpaStorage;

    public Collection<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa findById(Long ratingId) {
        return mpaStorage.findMpaById(ratingId)
                .orElseThrow(() -> {
                    log.warn("Рейтинг с id:{} не найден", ratingId);
                    return new NotFoundException("Рейтинг MPA не найден");
                });
    }
}