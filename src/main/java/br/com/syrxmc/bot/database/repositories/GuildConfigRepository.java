package br.com.syrxmc.bot.database.repositories;

import br.com.syrxmc.bot.domain.guild.GuildConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuildConfigRepository {

    private final MongoCollection<Document> collection;

    public GuildConfigRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Optional<GuildConfig> findByGuildId(String guildId) {
        Document doc = collection.find(Filters.eq("guildId", guildId)).first();
        return Optional.ofNullable(doc).map(this::fromDocument);
    }

    public void save(GuildConfig config) {
        Document doc = toDocument(config);
        collection.replaceOne(
                Filters.eq("guildId", config.getGuildId()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public void updateLastMenuMessageId(String guildId, String messageId) {
        collection.updateOne(
                Filters.eq("guildId", guildId),
                Updates.set("messages.lastMenuMessageId", messageId)
        );
    }

    public void updateLastLeaderboardMessageId(String guildId, String messageId) {
        collection.updateOne(
                Filters.eq("guildId", guildId),
                Updates.set("messages.lastLeaderboardMessageId", messageId)
        );
    }

    public void updateLastGoldStockMessageId(String guildId, String messageId) {
        collection.updateOne(
                Filters.eq("guildId", guildId),
                Updates.set("messages.lastGoldStockMessageId", messageId)
        );
    }

    public void setInviteEventActive(String guildId, boolean active) {
        collection.updateOne(
                Filters.eq("guildId", guildId),
                Updates.set("inviteEventActive", active)
        );
    }

    private Document toDocument(GuildConfig config) {
        Document doc = new Document();
        if (config.getId() != null) {
            doc.put("_id", config.getId());
        }
        doc.put("guildId", config.getGuildId());
        doc.put("token", config.getToken());
        doc.put("ticketCategoryId", config.getTicketCategoryId());
        doc.put("inviteEventActive", config.isInviteEventActive());
        doc.put("ignoredUserIds", config.getIgnoredUserIds() != null ? config.getIgnoredUserIds() : new ArrayList<>());
        doc.put("greetMessage", config.getGreetMessage());
        doc.put("greetImageUrl", config.getGreetImageUrl());
        doc.put("color", config.getColor());

        if (config.getChannels() != null) {
            Document channels = new Document();
            channels.put("menu", config.getChannels().getMenu());
            channels.put("info", config.getChannels().getInfo());
            channels.put("greet", config.getChannels().getGreet());
            channels.put("invite", config.getChannels().getInvite());
            channels.put("logsCash", config.getChannels().getLogsCash());
            channels.put("logsGold", config.getChannels().getLogsGold());
            doc.put("channels", channels);
        }

        if (config.getRoles() != null) {
            Document roles = new Document();
            roles.put("cash", config.getRoles().getCash());
            roles.put("gold", config.getRoles().getGold());
            roles.put("intermedio", config.getRoles().getIntermedio());
            doc.put("roles", roles);
        }

        if (config.getMessages() != null) {
            Document messages = new Document();
            messages.put("lastMenuMessageId", config.getMessages().getLastMenuMessageId());
            messages.put("lastLeaderboardMessageId", config.getMessages().getLastLeaderboardMessageId());
            messages.put("lastGoldStockMessageId", config.getMessages().getLastGoldStockMessageId());
            doc.put("messages", messages);
        }

        return doc;
    }

    @SuppressWarnings("unchecked")
    private GuildConfig fromDocument(Document doc) {
        GuildConfig config = new GuildConfig();
        config.setId(doc.getObjectId("_id"));
        config.setGuildId(doc.getString("guildId"));
        config.setToken(doc.getString("token"));
        config.setTicketCategoryId(doc.getString("ticketCategoryId"));
        config.setInviteEventActive(Boolean.TRUE.equals(doc.getBoolean("inviteEventActive")));
        List<String> ignoredIds = (List<String>) doc.get("ignoredUserIds");
        config.setIgnoredUserIds(ignoredIds != null ? ignoredIds : new ArrayList<>());
        config.setGreetMessage(doc.getString("greetMessage"));
        config.setGreetImageUrl(doc.getString("greetImageUrl"));
        config.setColor(doc.getString("color"));

        Document channelsDoc = (Document) doc.get("channels");
        if (channelsDoc != null) {
            GuildConfig.Channels channels = new GuildConfig.Channels();
            channels.setMenu(channelsDoc.getString("menu"));
            channels.setInfo(channelsDoc.getString("info"));
            channels.setGreet(channelsDoc.getString("greet"));
            channels.setInvite(channelsDoc.getString("invite"));
            channels.setLogsCash(channelsDoc.getString("logsCash"));
            channels.setLogsGold(channelsDoc.getString("logsGold"));
            config.setChannels(channels);
        }

        Document rolesDoc = (Document) doc.get("roles");
        if (rolesDoc != null) {
            GuildConfig.Roles roles = new GuildConfig.Roles();
            roles.setCash(rolesDoc.getString("cash"));
            roles.setGold(rolesDoc.getString("gold"));
            roles.setIntermedio(rolesDoc.getString("intermedio"));
            config.setRoles(roles);
        }

        Document messagesDoc = (Document) doc.get("messages");
        if (messagesDoc != null) {
            GuildConfig.Messages messages = new GuildConfig.Messages();
            messages.setLastMenuMessageId(messagesDoc.getString("lastMenuMessageId"));
            messages.setLastLeaderboardMessageId(messagesDoc.getString("lastLeaderboardMessageId"));
            messages.setLastGoldStockMessageId(messagesDoc.getString("lastGoldStockMessageId"));
            config.setMessages(messages);
        } else {
            config.setMessages(new GuildConfig.Messages());
        }

        return config;
    }
}
