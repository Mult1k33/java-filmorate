package ru.yandex.practicum.filmorate.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public String getName() {
        return (name == null || name.isBlank()) ? login : name;
    }
}