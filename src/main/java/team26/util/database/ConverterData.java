package team26.util.database;

import team26.domain.user.User;
import team26.domain.user.UserRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ConverterData {

    public static User convertDataToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getObject("id", UUID.class));
        user.setName(rs.getString("name"));
        user.setSurname(rs.getString("surname"));
        user.setLogin(rs.getString("login"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setHashedPassword(rs.getString("hashed_password"));
        user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return user;
    }

}
