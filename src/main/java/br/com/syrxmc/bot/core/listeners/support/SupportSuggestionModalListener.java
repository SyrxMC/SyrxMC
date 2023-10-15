package br.com.syrxmc.bot.core.listeners.support;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.awt.*;

public class SupportSuggestionModalListener extends DynamicHandler<ModalInteractionEvent> {

    private final Config config;

    public SupportSuggestionModalListener(Config config) {
        super(event -> event.getModalId().contains("suggestion"));
        this.config = config;
    }

    @Override
    public void onEvent(ModalInteractionEvent event) {
        String type = event.getModalId().split(":")[1];

        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(Color.decode("#282b30"));

        event.deferReply(true).complete().deleteOriginal().queue();

        boolean isValidUrl = event.getValue("mod").getAsString().contains("https://");

        builder.setAuthor(String.format("Sugerido por %s", event.getMember().getAsMention()));
        builder.setDescription("Sugestão de **" + type.toUpperCase() + "**");
        if(type.equals("mod")) {
            builder.addField("Mod:", isValidUrl ? "[link](" + event.getValue("mod").getAsString() + ")" : event.getValue("mod").getAsString(), false);
        }
        builder.addField("Descrição:", String.format("```%s```", event.getValue("text").getAsString()), false);

        event.getGuild().getChannelById(TextChannel.class, config.getSuggestionChannelId()).sendMessageEmbeds(builder.build()).queue(message -> {
            message.createThreadChannel("Sugestão do " + event.getMember().getEffectiveName()).queue();
        });
    }
}
