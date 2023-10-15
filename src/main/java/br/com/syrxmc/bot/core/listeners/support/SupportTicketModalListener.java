package br.com.syrxmc.bot.core.listeners.support;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SupportTicketModalListener extends DynamicHandler<ModalInteractionEvent> {

    private final Config config;

    public SupportTicketModalListener(Config config) {
        super(event -> event.getModalId().equals("ticket"));
        this.config = config;
    }

    @Override
    public void onEvent(ModalInteractionEvent event) {
        event.deferReply(true).complete().deleteOriginal().queue();

        Button closeButton = Button.of(ButtonStyle.DANGER, "closeTicket", "Fechar Ticket");
        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(Color.decode("#282b30"));

        builder.setTitle("Ticket aberto por " + event.getMember().getEffectiveName());
        builder.addField("Descrição:", "```" + event.getValue("text").getAsString() + "```", false);

        List<Permission> memberPermissionsDefault = List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_EXT_STICKER, Permission.MESSAGE_EXT_EMOJI, Permission.VIEW_CHANNEL);

        Role role = Objects.requireNonNull(event.getGuild()).getPublicRole();

        event.getGuild().getCategoryById(config.getTicketCategoryId()).createTextChannel(event.getValue("nikcname").getAsString())
                .addMemberPermissionOverride(event.getMember().getIdLong(), memberPermissionsDefault, new ArrayList<>())
                .removePermissionOverride(role)
                .removePermissionOverride("1160294617799999570") // Member Role
                .queue(textChannel -> {
                    textChannel.sendMessageEmbeds(builder.build()).addActionRow(closeButton).queue();
                    textChannel.sendMessage("Olá " + event.getMember().getAsMention() + ", você solicitou o suporte da <@&1163229278511583232>.\n" +
                            "Caso tenha mais informações sobre o que você precisa, nos diga abaixo e logo iremos te responder.").queue();
                });
    }
}
