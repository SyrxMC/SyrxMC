package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.SlashSubcommand;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.ticket.Ticket;
import br.com.syrxmc.bot.domain.ticket.TicketService;
import br.com.syrxmc.bot.domain.ticket.TicketType;
import br.com.syrxmc.bot.utils.SyrxEmbeds;
import br.com.syrxmc.bot.utils.WriteChannelBackup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RegisterCommand
public class CloseCommand extends SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(CloseCommand.class);

    public CloseCommand() {
        super("fechar", "Fechar a salas de tickets");
        addSubcommand(new CloseCash());
        addSubcommand(new CloseIntermedio());
        addSubcommand(new CloseGold());
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        // Subcommands handle execution
    }

    private static void performClose(SlashCommandInteractionEvent event, TicketType expectedType,
                                     String logsChannelOverride, Double saleValue) {
        TextChannel textChannel = event.getChannel().asTextChannel();
        String channelId = textChannel.getId();
        String guildId = event.getGuild().getId();

        TicketService ticketService = ServiceRegistry.getTicketService();
        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();

        Optional<Ticket> ticketOpt = ticketService.findByChannel(channelId);
        if (ticketOpt.isEmpty() || !expectedType.equals(ticketOpt.get().getType())) {
            String typeName = expectedType.name().toLowerCase();
            event.reply("O canal que você está tentando fechar não é de " + typeName)
                    .setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        Ticket ticket = ticketOpt.get();

        // Backup the channel
        try {
            WriteChannelBackup.writeFile(textChannel, "/tickets/" + ticket.getType().name());
        } catch (Exception e) {
            logger.warn("Failed to backup channel {}", channelId, e);
        }

        // Close in DB
        String closedBy = event.getUser().getId();
        try {
            ticketService.close(channelId, closedBy, saleValue);
        } catch (Exception e) {
            logger.error("Failed to close ticket in DB for channel {}", channelId, e);
        }

        // Send log message
        String logsChannelId = logsChannelOverride;
        if (logsChannelId == null) {
            try {
                GuildConfig config = guildConfigService.getConfig(guildId);
                if (config.getChannels() != null) {
                    logsChannelId = switch (expectedType) {
                        case CASH -> config.getChannels().getLogsCash();
                        case GOLD -> config.getChannels().getLogsGold();
                        default -> null;
                    };
                }
            } catch (Exception e) {
                logger.warn("Could not get logs channel from config: {}", e.getMessage());
            }
        }

        if (logsChannelId != null) {
            TextChannel logsChannel = event.getGuild().getChannelById(TextChannel.class, logsChannelId);
            if (logsChannel != null) {
                logsChannel.sendMessageEmbeds(SyrxEmbeds.ticketClosed(closedBy, ticket.getUserId(), saleValue)).queue();
            }
        }

        textChannel.sendMessage("Encerrando ticket em 5s.").queue();
        textChannel.delete().queueAfter(5, TimeUnit.SECONDS);
    }

    public static class CloseCash extends SlashSubcommand {

        public CloseCash() {
            super("cash", "Fechar a sala de cash");
            addOption(new OptionData(OptionType.STRING, "valor", "Valor do cash que foi vendido", false));
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            OptionMapping valorOption = event.getOption("valor");
            Double saleValue = null;
            if (valorOption != null) {
                try {
                    saleValue = Double.parseDouble(valorOption.getAsString());
                } catch (NumberFormatException e) {
                    // keep null
                }
            }
            performClose(event, TicketType.CASH, null, saleValue);
        }
    }

    public static class CloseIntermedio extends SlashSubcommand {

        public CloseIntermedio() {
            super("intermedio", "Fechar a sala de intermedio");
            addOption(new OptionData(OptionType.NUMBER, "valor", "Valor do intermédio", false));
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            OptionMapping valorOption = event.getOption("valor");
            Double saleValue = valorOption != null ? valorOption.getAsDouble() : null;
            performClose(event, TicketType.INTERMEDIO, null, saleValue);
        }
    }

    public static class CloseGold extends SlashSubcommand {

        public CloseGold() {
            super("gold", "Fechar a sala de gold");
            addOption(new OptionData(OptionType.NUMBER, "valor", "Valor de gold vendida", false));
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            OptionMapping valorOption = event.getOption("valor");
            Double saleValue = valorOption != null ? valorOption.getAsDouble() : null;
            performClose(event, TicketType.GOLD, null, saleValue);
        }
    }
}
