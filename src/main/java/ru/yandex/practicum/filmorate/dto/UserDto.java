package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Long> friends;

    public String getName() {
        return (name == null || name.isBlank()) ? login : name;
    }
}