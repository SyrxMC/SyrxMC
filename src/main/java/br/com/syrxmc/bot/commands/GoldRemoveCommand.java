package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.gold.GoldStock;
import br.com.syrxmc.bot.domain.gold.GoldStockService;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.utils.SyrxEmbeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static br.com.syrxmc.bot.utils.Utils.convertToShortScale;

@RegisterCommand
public class GoldRemoveCommand extends SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(GoldRemoveCommand.class);

    public GoldRemoveCommand() {
        super("removegold", "Remove o gold");
        addOption(new OptionData(OptionType.STRING, "servidor", "Servidor do gold", true));
        addOption(new OptionData(OptionType.INTEGER, "quantidade", "Quantidade de gold", true));
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String server = event.getStringOption("servidor").toUpperCase();
        long quantity = event.getLongOption("quantidade");
        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        GoldStockService goldStockService = ServiceRegistry.getGoldStockService();
        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();

        goldStockService.remove(guildId, server, quantity, userId);

        List<GoldStock> allStocks = goldStockService.getAll(guildId);
        long newAmount = allStocks.stream()
                .filter(s -> server.equals(s.getServerName()))
                .mapToLong(GoldStock::getAmount)
                .findFirst()
                .orElse(0L);

        event.reply("Foi removido **%s** de gold no bloco do **%s**. Saldo atual: ***%s*** - __**%s**__",
                convertToShortScale(quantity), server, convertToShortScale(newAmount), newAmount)
                .setEphemeral(true).queue();

        // Refresh gold stock display
        try {
            GuildConfig config = guildConfigService.getConfig(guildId);
            if (config.getChannels() != null && config.getChannels().getInfo() != null) {
                TextChannel infoChannel = event.getGuild().getChannelById(TextChannel.class, config.getChannels().getInfo());
                if (infoChannel != null) {
                    String color = config.getColor();
                    String lastMsgId = config.getMessages() != null ? config.getMessages().getLastGoldStockMessageId() : null;
                    MessageEmbed embed = allStocks.isEmpty()
                            ? SyrxEmbeds.goldStockEmpty(color)
                            : SyrxEmbeds.goldStockDisplay(allStocks, color);
                    if (lastMsgId != null) {
                        infoChannel.editMessageEmbedsById(lastMsgId, embed).queue(
                                success -> {},
                                err -> infoChannel.sendMessageEmbeds(embed).queue(msg ->
                                        guildConfigService.updateLastGoldStockMessageId(guildId, msg.getId()))
                        );
                    } else {
                        infoChannel.sendMessageEmbeds(embed).queue(msg ->
                                guildConfigService.updateLastGoldStockMessageId(guildId, msg.getId()));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not refresh gold stock display: {}", e.getMessage());
        }
    }
}
