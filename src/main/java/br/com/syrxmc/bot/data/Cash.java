package br.com.syrxmc.bot.data;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Cash {

    private Map<String, List<Ticket>> tickets = new HashMap<>();

    public enum TicketType {
        CASH, INTERMEDIO, GOLD
    }

    public record Ticket(String creatorId, String channelId, TicketType type) {
    }

}
