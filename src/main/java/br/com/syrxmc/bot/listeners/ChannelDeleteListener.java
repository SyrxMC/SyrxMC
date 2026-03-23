package br.com.syrxmc.bot.listeners;

import br.com.syrxmc.bot.domain.ticket.TicketService;
import br.com.syrxmc.bot.domain.ticket.TicketStatus;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelDeleteListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChannelDeleteListener.class);

    private final TicketService ticketService;

    public ChannelDeleteListener(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        String channelId = event.getChannel().getId();

        ticketService.findByChannel(channelId).ifPresent(ticket -> {
            if (TicketStatus.OPEN.equals(ticket.getStatus())) {
                try {
                    ticketService.close(channelId, null, null);
                    logger.info("Ticket do canal {} ({}) fechado após exclusão manual do canal.", channelId, ticket.getType());
                } catch (Exception e) {
                    logger.error("Erro ao fechar ticket após exclusão do canal {}", channelId, e);
                }
            }
        });
    }
}
