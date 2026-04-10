package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

import static br.com.syrxmc.bot.utils.UtilsStatics.PRIMARY_COLOR;

@RegisterCommand
public class CashMenuCommand extends SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(CashMenuCommand.class);

    public CashMenuCommand() {
        super("cashmenu", "Create cash menu", true);
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.BOOLEAN, "gold", "gerar botão de gold"));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        event.defer().setEphemeral(true).complete().deleteOriginal().queue();

        String guildId = event.getGuild().getId();
        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();

        Color color = PRIMARY_COLOR;
        try {
            GuildConfig config = guildConfigService.getConfig(guildId);
            if (config.getColor() != null && !config.getColor().isBlank()) {
                color = Color.decode(config.getColor());
            }
        } catch (Exception e) {
            logger.warn("Could not load guild config for cashmenu: {}", e.getMessage());
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Cash");
        builder.setDescription("Selecione abaixo a opção desejada");
        builder.setFooter("Cuidado com vendas não autorizadas de terceiros!");
        builder.setColor(color);
        builder.setImage("https://amplologistica.com.br/wp-content/uploads/2018/02/ecommerce-subway-studio-malaysia.gif");

        MessageCreateAction messageCreateAction = null;

        if(event.getBooleanOption("gold")) {
            messageCreateAction = event.getChannel().sendMessageEmbeds(builder.build()).addComponents(
                    ActionRow.of(Button.secondary("open_ticket:CASH", "QUERO CASH").withEmoji(Emoji.fromUnicode("\uD83D\uDCB0")),
                            Button.secondary("open_ticket:INTERMEDIO", "INTERMÉDIO").withEmoji(Emoji.fromUnicode("\uD83E\uDD1D")),
                            Button.secondary("open_ticket:GOLD", "QUERO GOLD").withEmoji(Emoji.fromUnicode("\uD83E\uDE99")))
                    );
        } else {
            messageCreateAction = event.getChannel().sendMessageEmbeds(builder.build()).addComponents(
                    ActionRow.of(Button.secondary("open_ticket:CASH", "QUERO CASH").withEmoji(Emoji.fromUnicode("\uD83D\uDCB0")),
                            Button.secondary("open_ticket:INTERMEDIO", "INTERMÉDIO").withEmoji(Emoji.fromUnicode("\uD83E\uDD1D")))
            );
        }


        messageCreateAction.queue(message -> {
            try {
                guildConfigService.updateLastMenuMessageId(guildId, message.getId());
            } catch (Exception e) {
                logger.warn("Could not update last menu message ID: {}", e.getMessage());
            }
        });
    }
}
