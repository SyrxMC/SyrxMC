package br.com.syrxmc.bot.database.repositories;

import br.com.syrxmc.bot.domain.gold.GoldStock;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoldStockRepository {

    private final MongoCollection<Document> collection;

    public GoldStockRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void upsert(String guildId, String serverName, long delta, String updatedBy) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.eq("serverName", serverName)
                ),
                Updates.combine(
                        Updates.inc("amount", delta),
                        Updates.set("updatedAt", Date.from(Instant.now())),
                        Updates.set("updatedBy", updatedBy),
                        Updates.setOnInsert("guildId", guildId),
                        Updates.setOnInsert("serverName", serverName),
                        Updates.setOnInsert("_id", new ObjectId())
                ),
                new UpdateOptions().upsert(true)
        );
    }

    public List<GoldStock> findAll(String guildId) {
        List<GoldStock> results = new ArrayList<>();
        collection.find(Filters.eq("guildId", guildId))
                .forEach(doc -> results.add(fromDocument(doc)));
        return results;
    }

    public void remove(String guildId, String serverName, long amount, String updatedBy) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.eq("serverName", serverName)
                ),
                Updates.combine(
                        Updates.inc("amount", -amount),
                        Updates.set("updatedAt", Date.from(Instant.now())),
                        Updates.set("updatedBy", updatedBy)
                )
        );
    }

    private GoldStock fromDocument(Document doc) {
        GoldStock gs = new GoldStock();
        gs.setId(doc.getObjectId("_id"));
        gs.setGuildId(doc.getString("guildId"));
        gs.setServerName(doc.getString("serverName"));
        Number amount = (Number) doc.get("amount");
        gs.setAmount(amount != null ? amount.longValue() : 0L);
        Date updatedAt = doc.getDate("updatedAt");
        if (updatedAt != null) gs.setUpdatedAt(updatedAt.toInstant());
        gs.setUpdatedBy(doc.getString("updatedBy"));
        return gs;
    }
}
