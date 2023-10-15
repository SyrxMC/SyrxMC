package br.com.syrxmc.bot.core.listeners.support;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.utils.WriteChannelBackup;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ButtonCloseTicketListener extends DynamicHandler<ButtonInteractionEvent> {

    private final Config config;

    private final static Logger logger = LoggerFactory.getLogger(ButtonCloseTicketListener.class);

    public ButtonCloseTicketListener(Config config) {
        super(event -> event.getComponentId().equals("closeTicket"));
        this.config = config;
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        event.deferReply(true).complete().deleteOriginal().queue();
        TextChannel textChannel = event.getChannel().asTextChannel();

        try {
            WriteChannelBackup.writeFile(textChannel, "/tickets");
        } catch (IOException e) {
            logger.error("Erro ao gerar aquivo de salvamento dos canal {}", event.getChannel().getName(), e);
        }

        textChannel.sendMessage("Deletando canal em 10s").queue(message -> {
            message.delete().queueAfter(10, TimeUnit.SECONDS, success -> {
                message.getChannel().delete().queue();
            });
            message.getGuild().getChannelById(TextChannel.class, config.getSystemLogChannelId())
                    .sendMessage("Ticket de `" + event.getChannel().getName() + "` fechado por ***" + event.getMember().getEffectiveName() + "***").queue();

        });
    }
}
