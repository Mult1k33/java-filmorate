package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Mpa mpa;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Long> likesByUsers;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Genre> genres;
}