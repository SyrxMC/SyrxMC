package br.com.syrxmc.bot.core;

import br.com.syrxmc.bot.core.command.CommandManager;
import br.com.syrxmc.bot.core.listeners.CashButtonListener;
import br.com.syrxmc.bot.core.listeners.CommandListener;
import br.com.syrxmc.bot.core.listeners.IntermedioButtonListener;
import br.com.syrxmc.bot.core.listeners.MemberJoinListener;
import br.com.syrxmc.bot.core.listeners.events.DynamicEventHandler;
import br.com.syrxmc.bot.data.Config;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

@Getter
public class SyrxCore {

    private final static Logger logger = LoggerFactory.getLogger(SyrxCore.class);
    private final JDA jda;
    private final Config config;
    private final CommandManager commandManager;
    private final EventWaiter eventWaiter = new EventWaiter();

    public SyrxCore(Config config) {
        this.config = config;
        this.jda = createBot();
        commandManager = new CommandManager(jda);
    }

    public void initialize() {
        commandManager.publicCommands();
        DynamicEventHandler.getInstance().addListener(new CommandListener(this));
        DynamicEventHandler.getInstance().addListener(new MemberJoinListener(config));
        DynamicEventHandler.getInstance().addListener(new CashButtonListener(config));
        DynamicEventHandler.getInstance().addListener(new IntermedioButtonListener(config));
    }

    private JDA createBot() {

        JDABuilder builder = JDABuilder.create(
                config.getToken(),
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS
                ).setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(DynamicEventHandler.getInstance(), eventWaiter)
                .disableCache(List.of(EMOJI, CLIENT_STATUS, ACTIVITY, SCHEDULED_EVENTS))
                .setActivity(Activity.customStatus("RogMarket - Selling Cash"));

        return builder.build();

    }

    public Guild getGuildById(String guildId) {
        return jda.getGuildById(guildId);
    }

    public <T extends Channel> T getChannelById(Class<T> type, String channelId) {
        return jda.getChannelById(type, channelId);
    }

}
