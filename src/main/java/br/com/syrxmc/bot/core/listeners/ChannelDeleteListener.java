package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Cash;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ChannelDeleteListener extends DynamicHandler<ChannelDeleteEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelDeleteListener.class);

    public ChannelDeleteListener() {
        super(event -> true);
    }

    @Override
    public void onEvent(ChannelDeleteEvent event) {
        String channelId = event.getChannel().getId();
        Cash cash = Main.getCashManager().get();

        Optional<Cash.Ticket> ticketOpt = cash.findByChannelId(channelId);

        if (ticketOpt.isEmpty()) return;

        Cash.Ticket ticket = ticketOpt.get();
        cash.removeTicket(ticket);

        try {
            Main.getCashManager().save(cash);
            Main.reloadConfig();
            logger.info("Ticket do canal {} ({}) removido após exclusão manual do canal.", channelId, ticket.type());
        } catch (Exception e) {
            logger.error("Erro ao remover ticket após exclusão do canal {}", channelId, e);
        }
    }
}
