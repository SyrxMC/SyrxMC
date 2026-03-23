package br.com.syrxmc.bot.listeners.buttons;

import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.ticket.TicketService;
import br.com.syrxmc.bot.utils.WriteChannelBackup;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TicketCloseButtonListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TicketCloseButtonListener.class);

    private final TicketService ticketService;
    private final GuildConfigService guildConfigService;

    public TicketCloseButtonListener(TicketService ticketService, GuildConfigService guildConfigService) {
        this.ticketService = ticketService;
        this.guildConfigService = guildConfigService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!"close_ticket_self".equals(event.getComponentId())) {
            return;
        }

        try {
            event.deferReply().complete().deleteOriginal().queue();

            List<Message> nonBotMessages = event.getChannel().getHistory()
                    .retrievePast(5).complete()
                    .stream()
                    .filter(message -> !message.getAuthor().isBot())
                    .toList();

            if (!nonBotMessages.isEmpty()) {
                event.getGuildChannel()
                        .editMessageEmbedsById(event.getInteraction().getMessageId(), event.getMessage().getEmbeds())
                        .setReplace(true).queue();
                event.getChannel().sendMessage(
                        "Você não pode apagar mais esse ticket. Entre em contato com alguém da staff para fecha-lo"
                ).queue();
                return;
            }

            String channelId = event.getChannel().getId();

            ticketService.findByChannel(channelId).ifPresent(ticket -> {
                try {
                    WriteChannelBackup.writeFile(event.getChannel().asTextChannel(), "/tickets/" + ticket.getType().name());
                } catch (Exception e) {
                    logger.warn("Failed to backup ticket channel {}", channelId, e);
                }
                try {
                    ticketService.close(channelId, null, null);
                } catch (Exception e) {
                    logger.warn("Failed to close ticket in DB for channel {}", channelId, e);
                }
            });

            event.getChannel().sendMessage("Encerrando ticket em 5s.").queue();
            event.getChannel().delete().queueAfter(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            logger.error("Error handling close_ticket_self button", e);
        }
    }
}
