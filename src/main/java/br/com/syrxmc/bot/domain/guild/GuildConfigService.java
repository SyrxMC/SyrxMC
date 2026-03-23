package br.com.syrxmc.bot.domain.guild;

import br.com.syrxmc.bot.database.repositories.GuildConfigRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildConfigService {

    private final GuildConfigRepository repository;
    private final Map<String, GuildConfig> cache = new ConcurrentHashMap<>();

    public GuildConfigService(GuildConfigRepository repository) {
        this.repository = repository;
    }

    public GuildConfig getConfig(String guildId) {
        if (cache.containsKey(guildId)) {
            return cache.get(guildId);
        }
        GuildConfig config = repository.findByGuildId(guildId)
                .orElseThrow(() -> new IllegalStateException("No guild config found for guildId: " + guildId));
        cache.put(guildId, config);
        return config;
    }

    public void updateLastMenuMessageId(String guildId, String messageId) {
        repository.updateLastMenuMessageId(guildId, messageId);
        if (cache.containsKey(guildId)) {
            GuildConfig config = cache.get(guildId);
            if (config.getMessages() == null) config.setMessages(new GuildConfig.Messages());
            config.getMessages().setLastMenuMessageId(messageId);
        }
    }

    public void updateLastGoldStockMessageId(String guildId, String messageId) {
        repository.updateLastGoldStockMessageId(guildId, messageId);
        if (cache.containsKey(guildId)) {
            GuildConfig config = cache.get(guildId);
            if (config.getMessages() == null) config.setMessages(new GuildConfig.Messages());
            config.getMessages().setLastGoldStockMessageId(messageId);
        }
    }

    public void updateLastLeaderboardMessageId(String guildId, String messageId) {
        repository.updateLastLeaderboardMessageId(guildId, messageId);
        if (cache.containsKey(guildId)) {
            GuildConfig config = cache.get(guildId);
            if (config.getMessages() == null) config.setMessages(new GuildConfig.Messages());
            config.getMessages().setLastLeaderboardMessageId(messageId);
        }
    }

    public void setInviteEventActive(String guildId, boolean active) {
        repository.setInviteEventActive(guildId, active);
        if (cache.containsKey(guildId)) {
            cache.get(guildId).setInviteEventActive(active);
        }
    }

    public GuildConfig initConfig(String guildId) {
        GuildConfig existing = repository.findByGuildId(guildId).orElse(null);
        if (existing != null) {
            cache.put(guildId, existing);
            return existing;
        }
        GuildConfig config = new GuildConfig();
        config.setGuildId(guildId);
        config.setChannels(new GuildConfig.Channels());
        config.setRoles(new GuildConfig.Roles());
        config.setMessages(new GuildConfig.Messages());
        repository.save(config);
        cache.put(guildId, config);
        return config;
    }

    public void updateChannels(String guildId, GuildConfig.Channels channels) {
        GuildConfig config = getConfig(guildId);
        GuildConfig.Channels current = config.getChannels() != null ? config.getChannels() : new GuildConfig.Channels();
        if (channels.getMenu() != null) current.setMenu(channels.getMenu());
        if (channels.getInfo() != null) current.setInfo(channels.getInfo());
        if (channels.getGreet() != null) current.setGreet(channels.getGreet());
        if (channels.getInvite() != null) current.setInvite(channels.getInvite());
        if (channels.getLogsCash() != null) current.setLogsCash(channels.getLogsCash());
        if (channels.getLogsGold() != null) current.setLogsGold(channels.getLogsGold());
        config.setChannels(current);
        repository.save(config);
        cache.put(guildId, config);
    }

    public void updateRoles(String guildId, GuildConfig.Roles roles) {
        GuildConfig config = getConfig(guildId);
        GuildConfig.Roles current = config.getRoles() != null ? config.getRoles() : new GuildConfig.Roles();
        if (roles.getCash() != null) current.setCash(roles.getCash());
        if (roles.getGold() != null) current.setGold(roles.getGold());
        if (roles.getIntermedio() != null) current.setIntermedio(roles.getIntermedio());
        config.setRoles(current);
        repository.save(config);
        cache.put(guildId, config);
    }

    public void updateTicketCategoryId(String guildId, String categoryId) {
        GuildConfig config = getConfig(guildId);
        config.setTicketCategoryId(categoryId);
        repository.save(config);
        cache.put(guildId, config);
    }

    public void updateColor(String guildId, String color) {
        GuildConfig config = getConfig(guildId);
        config.setColor(color);
        repository.save(config);
        cache.put(guildId, config);
    }

    public void reload(String guildId) {
        cache.remove(guildId);
        GuildConfig config = repository.findByGuildId(guildId)
                .orElseThrow(() -> new IllegalStateException("No guild config found for guildId: " + guildId));
        cache.put(guildId, config);
    }
}
