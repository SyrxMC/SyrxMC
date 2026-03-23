package br.com.syrxmc.bot.database.repositories;

import br.com.syrxmc.bot.domain.invite.InviteData;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class InviteRepository {

    private final MongoCollection<Document> collection;

    public InviteRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void upsertInviter(String guildId, String inviteCode, String inviterUserId) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.eq("inviteCode", inviteCode)
                ),
                Updates.combine(
                        Updates.setOnInsert("_id", new ObjectId()),
                        Updates.setOnInsert("guildId", guildId),
                        Updates.setOnInsert("inviteCode", inviteCode),
                        Updates.setOnInsert("inviterUserId", inviterUserId),
                        Updates.setOnInsert("count", 0),
                        Updates.setOnInsert("invitedUserIds", new ArrayList<>()),
                        Updates.set("updatedAt", Date.from(Instant.now()))
                ),
                new UpdateOptions().upsert(true)
        );
    }

    public void recordJoin(String guildId, String inviteCode, String newUserId) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.eq("inviteCode", inviteCode)
                ),
                Updates.combine(
                        Updates.inc("count", 1),
                        Updates.push("invitedUserIds", newUserId),
                        Updates.set("updatedAt", Date.from(Instant.now()))
                )
        );
    }

    public void recordLeave(String guildId, String userId) {
        Document inviteDoc = collection.find(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.in("invitedUserIds", userId)
                )
        ).first();

        if (inviteDoc == null) return;

        collection.updateOne(
                Filters.eq("_id", inviteDoc.getObjectId("_id")),
                Updates.combine(
                        Updates.inc("count", -1),
                        Updates.pull("invitedUserIds", userId),
                        Updates.set("updatedAt", Date.from(Instant.now()))
                )
        );
    }

    public List<InviteData> getLeaderboard(String guildId, int limit) {
        List<InviteData> results = new ArrayList<>();
        collection.find(Filters.eq("guildId", guildId))
                .sort(Sorts.descending("count"))
                .limit(limit)
                .forEach(doc -> results.add(fromDocument(doc)));
        return results;
    }

    public Optional<InviteData> findByCode(String guildId, String inviteCode) {
        Document doc = collection.find(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.eq("inviteCode", inviteCode)
                )
        ).first();
        return Optional.ofNullable(doc).map(this::fromDocument);
    }

    public Optional<InviteData> findByInvitedUser(String guildId, String userId) {
        Document doc = collection.find(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.in("invitedUserIds", userId)
                )
        ).first();
        return Optional.ofNullable(doc).map(this::fromDocument);
    }

    @SuppressWarnings("unchecked")
    private InviteData fromDocument(Document doc) {
        InviteData data = new InviteData();
        data.setId(doc.getObjectId("_id"));
        data.setGuildId(doc.getString("guildId"));
        data.setInviterUserId(doc.getString("inviterUserId"));
        data.setInviteCode(doc.getString("inviteCode"));
        Number count = (Number) doc.get("count");
        data.setCount(count != null ? count.intValue() : 0);
        List<String> invitedUserIds = (List<String>) doc.get("invitedUserIds");
        data.setInvitedUserIds(invitedUserIds != null ? invitedUserIds : new ArrayList<>());
        Date updatedAt = doc.getDate("updatedAt");
        if (updatedAt != null) data.setUpdatedAt(updatedAt.toInstant());
        return data;
    }
}
