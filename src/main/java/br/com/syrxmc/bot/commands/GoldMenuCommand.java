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

import java.util.List;

@RegisterCommand
public class GoldMenuCommand extends SlashCommand {

    public GoldMenuCommand() {
        super("goldmenu", "Envia o display de estoque de gold no canal de informações");
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        event.defer().setEphemeral(true).complete().deleteOriginal().queue();

        String guildId = event.getGuild().getId();
        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();
        GoldStockService goldStockService = ServiceRegistry.getGoldStockService();

        GuildConfig config = guildConfigService.getConfig(guildId);

        if (config.getChannels() == null || config.getChannels().getInfo() == null) {
            event.getChannel().sendMessage("Canal de informações não configurado. Use `/config-canais info #canal`.")
                    .queue(m -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
            return;
        }

        TextChannel infoChannel = event.getGuild().getChannelById(TextChannel.class, config.getChannels().getInfo());
        if (infoChannel == null) {
            event.getChannel().sendMessage("Canal de informações não encontrado.")
                    .queue(m -> m.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
            return;
        }

        List<GoldStock> stocks = goldStockService.getAll(guildId);
        MessageEmbed embed = stocks.isEmpty()
                ? SyrxEmbeds.goldStockEmpty(config.getColor())
                : SyrxEmbeds.goldStockDisplay(stocks, config.getColor());

        // Delete old message if exists
        String lastMsgId = config.getMessages() != null ? config.getMessages().getLastGoldStockMessageId() : null;
        if (lastMsgId != null) {
            infoChannel.deleteMessageById(lastMsgId).queue(v -> {}, err -> {});
        }

        infoChannel.sendMessageEmbeds(embed).queue(msg ->
                guildConfigService.updateLastGoldStockMessageId(guildId, msg.getId())
        );
    }
}
