package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Likes;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LikesRowMapper implements RowMapper<Likes> {

    @Override
    public Likes mapRow(ResultSet rs, int rowNum) throws SQLException {
        Likes likes = new Likes();
        likes.setFilmId(rs.getLong("film_id"));
        likes.setUserId(rs.getLong("user_id"));
        return likes;
    }
}