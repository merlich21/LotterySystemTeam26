package ru.mephi.team26.dto.ticket;

import lombok.Getter;
import lombok.Setter;
import ru.mephi.team26.entity.TicketStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class TicketResponseDto {
    private long id;
    private long drawId;
    private long userId;
    private List<Integer> numbers;
    private TicketStatus status;
    private OffsetDateTime createdAt;
}
