package team26.util.database;

import team26.domain.lotteryDraw.LotteryDraw;
import team26.domain.lotteryDraw.LotteryDrawStatus;
import team26.domain.lotteryTicket.LotteryTicket;
import team26.domain.lotteryTicket.LotteryTicketStatus;
import team26.domain.user.User;
import team26.domain.user.UserRole;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public static List<User> convertDataToAllUsers(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();

        while (rs.next()) {
            users.add(ConverterData.convertDataToUser(rs));
        }

        return users;
    }

    public static LotteryDraw convertDataToLotteryDraw(ResultSet rs) throws SQLException {
        LotteryDraw draw = new LotteryDraw();
        draw.setId(rs.getObject("id", UUID.class));
        draw.setDrawNumber(rs.getInt("draw_number"));
        draw.setDrawName(rs.getString("draw_name"));
        draw.setTotalTickets(rs.getInt("total_tickets"));
        draw.setStatus(LotteryDrawStatus.valueOf(rs.getString("status")));
        draw.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return draw;
    }

    public static LotteryTicket convertDataToLotteryTicket(ResultSet rs) throws SQLException {
        LotteryTicket ticket = new LotteryTicket();
        ticket.setId(rs.getObject("id", UUID.class));
        ticket.setStatus(LotteryTicketStatus.valueOf(rs.getString("status")));
        ticket.setCreateAt(rs.getObject("created_at", OffsetDateTime.class));

        // Массив чисел
        Array numbersArray = rs.getArray("ticket_numbers");
        if (numbersArray != null) {
            Integer[] numbers = (Integer[]) numbersArray.getArray();

            ticket.setTicketNumbers(numbers);
        }


        UUID userId = rs.getObject("user_id", UUID.class);
        UUID drawId = rs.getObject("lottery_draw_id", UUID.class);

        return ticket;
    }

}
