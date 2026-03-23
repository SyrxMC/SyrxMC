package br.com.syrxmc.bot.domain.ticket;

import br.com.syrxmc.bot.database.repositories.TicketRepository;

import java.time.Instant;
import java.util.Optional;

public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public boolean hasOpenTicket(String userId, TicketType type, String guildId) {
        return ticketRepository.findOpenByUserAndType(userId, type, guildId).isPresent();
    }

    public Ticket create(String userId, String channelId, TicketType type, String guildId) {
        Ticket ticket = Ticket.builder()
                .userId(userId)
                .channelId(channelId)
                .type(type)
                .guildId(guildId)
                .status(TicketStatus.OPEN)
                .openedAt(Instant.now())
                .build();
        return ticketRepository.insert(ticket);
    }

    public Optional<Ticket> findByChannel(String channelId) {
        return ticketRepository.findByChannelId(channelId);
    }

    public Ticket close(String channelId, String closedBy, Double saleValue) {
        Ticket ticket = ticketRepository.findByChannelId(channelId)
                .orElseThrow(() -> new IllegalArgumentException("No ticket found for channel: " + channelId));
        ticketRepository.close(ticket.getId(), closedBy, saleValue);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedBy(closedBy);
        ticket.setSaleValue(saleValue);
        ticket.setClosedAt(Instant.now());
        return ticket;
    }
}
