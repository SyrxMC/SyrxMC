package br.com.syrxmc.bot.data;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class Cash {

    private Map<String, List<Ticket>> tickets = new HashMap<>();

    public enum TicketType {
        CASH, INTERMEDIO, GOLD
    }

    public record Ticket(String creatorId, String channelId, TicketType type) {
    }

    public Optional<Ticket> findByChannelId(String channelId) {
        return tickets.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.channelId().equals(channelId))
                .findFirst();
    }

    public boolean removeTicket(Ticket ticket) {
        List<Ticket> list = tickets.get(ticket.creatorId());
        if (list == null) return false;
        boolean removed = list.remove(ticket);
        tickets.put(ticket.creatorId(), list);
        return removed;
    }

}
