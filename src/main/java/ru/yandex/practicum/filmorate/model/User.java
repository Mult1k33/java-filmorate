package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;

/**
 * User.
 */
@Data
@EqualsAndHashCode(of = {"id"})
public class User {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friends;

    public String getName() {
        return (name == null || name.isBlank()) ? login : name;
    }
}