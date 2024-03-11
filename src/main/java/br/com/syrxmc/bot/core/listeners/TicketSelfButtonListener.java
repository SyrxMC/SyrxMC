package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.utils.WriteChannelBackup;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TicketSelfButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    private final Config config;

    public TicketSelfButtonListener(Config config) {
        super(event -> Objects.equals(event.getButton().getId(), "closeSelf"));
        this.config = config;
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        try {
            event.deferReply().complete().deleteOriginal().queue();

            List<Message> list = event.getChannel().getHistory().retrievePast(5).complete().stream().filter(message -> !message.getAuthor().isBot()).toList();

            if (!list.isEmpty()) {
                event.getGuildChannel().editMessageEmbedsById(event.getInteraction().getMessageId(), event.getMessage().getEmbeds())
                        .setReplace(true).queue();
                event.getChannel().sendMessage("Você não pode apagar mais esse ticket. Entre em contato com alguém da staff para fecha-lo").queue();
                return;
            }

            Cash cash = Main.getCash();

            Cash.Ticket ticket = null;

            for (List<Cash.Ticket> value : cash.getTickets().values()) {
                for (Cash.Ticket ticket1 : value) {
                    if (ticket1.channelId().equals(event.getChannel().getId())) {
                        ticket = ticket1;
                        break;
                    }
                }
            }

            WriteChannelBackup.writeFile(event.getChannel().asTextChannel(), "/tickets/" + ticket.type().name());

            List<Cash.Ticket> tickets = cash.getTickets().get(ticket.creatorId());

            tickets.remove(ticket);
            cash.getTickets().put(ticket.creatorId(), tickets);

            Main.getCashManager().save(cash);
            Main.reloadConfig();

            event.getChannel().sendMessage("Encerrando ticket em 5s.").queue();
            event.getChannel().delete().queueAfter(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
