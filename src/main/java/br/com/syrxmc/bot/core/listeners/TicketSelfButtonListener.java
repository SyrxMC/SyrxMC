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
import java.util.Optional;
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

            List<Message> nonBotMessages = event.getChannel().getHistory()
                    .retrievePast(5).complete().stream()
                    .filter(message -> !message.getAuthor().isBot())
                    .toList();

            if (!nonBotMessages.isEmpty()) {
                event.getGuildChannel()
                        .editMessageEmbedsById(event.getInteraction().getMessageId(), event.getMessage().getEmbeds())
                        .setReplace(true).queue();
                event.getChannel().sendMessage("Você não pode fechar este ticket pois já há mensagens de atendimento. Entre em contato com a staff para fechá-lo.").queue();
                return;
            }

            Cash cash = Main.getCash();
            Optional<Cash.Ticket> ticketOpt = cash.findByChannelId(event.getChannel().getId());

            if (ticketOpt.isEmpty()) {
                event.getChannel().sendMessage("Ticket não encontrado. Entre em contato com a staff.").queue();
                return;
            }

            Cash.Ticket ticket = ticketOpt.get();

            WriteChannelBackup.writeFile(event.getChannel().asTextChannel(), "/tickets/" + ticket.type().name());

            cash.removeTicket(ticket);
            Main.getCashManager().save(cash);
            Main.reloadConfig();

            event.getChannel().sendMessage("Encerrando ticket em 5s.").queue();
            event.getChannel().delete().queueAfter(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}