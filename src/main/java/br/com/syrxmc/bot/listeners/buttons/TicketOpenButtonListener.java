package br.com.syrxmc.bot.listeners.buttons;

import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.ticket.TicketService;
import br.com.syrxmc.bot.domain.ticket.TicketType;
import br.com.syrxmc.bot.utils.SyrxEmbeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

import static br.com.syrxmc.bot.core.listeners.PermissionsConstants.ALLOWED_PERMISSIONS;

public class TicketOpenButtonListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TicketOpenButtonListener.class);

    private final TicketService ticketService;
    private final GuildConfigService guildConfigService;

    public TicketOpenButtonListener(TicketService ticketService, GuildConfigService guildConfigService) {
        this.ticketService = ticketService;
        this.guildConfigService = guildConfigService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("open_ticket:")) {
            return;
        }

        String typeStr;
        try {
            typeStr = componentId.split(":")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }

        TicketType type;
        try {
            type = TicketType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            event.reply("Tipo de ticket inválido.").setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();
        String guildId = event.getGuild().getId();

        GuildConfig config;
        try {
            config = guildConfigService.getConfig(guildId);
        } catch (Exception e) {
            logger.error("Failed to get guild config for guildId: {}", guildId, e);
            event.reply("Erro ao obter configuração do servidor.").setEphemeral(true).queue();
            return;
        }

        if (ticketService.hasOpenTicket(userId, type, guildId)) {
            String typeName = type.name().charAt(0) + type.name().substring(1).toLowerCase();
            event.reply("Você já tem uma sala de " + typeName + " aberta!").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        String channelPrefix;
        switch (type) {
            case CASH -> channelPrefix = "CASH-";
            case GOLD -> channelPrefix = "gold-";
            case INTERMEDIO -> channelPrefix = "INTERMÉDIO-";
            default -> channelPrefix = "ticket-";
        }

        String categoryId = config.getTicketCategoryId();
        TextChannel createdChannel;
        try {
            var channelAction = event.getGuild().getCategoryById(categoryId)
                    .createTextChannel(channelPrefix + event.getMember().getEffectiveName())
                    .addMemberPermissionOverride(
                            event.getMember().getIdLong(),
                            ALLOWED_PERMISSIONS,
                            EnumSet.noneOf(Permission.class)
                    )
                    .addRolePermissionOverride(
                            event.getGuild().getPublicRole().getIdLong(),
                            EnumSet.noneOf(Permission.class),
                            EnumSet.of(Permission.VIEW_CHANNEL)
                    );
            createdChannel = channelAction.complete();
        } catch (Exception e) {
            logger.error("Failed to create ticket channel", e);
            return;
        }

        try {
            ticketService.create(userId, createdChannel.getId(), type, guildId);
        } catch (Exception e) {
            logger.error("Failed to register ticket in DB", e);
            createdChannel.delete().queue();
            return;
        }

        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, createdChannel.getId());
        textChannel.sendMessageEmbeds(SyrxEmbeds.ticketWelcome(type, event.getMember().getAsMention(), config))
                .addComponents(
                        ActionRow.of(Button.danger("close_ticket_self", "FECHAR TICKET").withEmoji(Emoji.fromUnicode("\u2716")))
                ).queue();
        textChannel.sendMessage(SyrxEmbeds.ticketWelcomeMessage(type, event.getMember().getAsMention(), config)).queue();
    }


}
