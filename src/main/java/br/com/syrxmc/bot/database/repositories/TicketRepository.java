package br.com.syrxmc.bot.database.repositories;

import br.com.syrxmc.bot.domain.ticket.Ticket;
import br.com.syrxmc.bot.domain.ticket.TicketStatus;
import br.com.syrxmc.bot.domain.ticket.TicketType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TicketRepository {

    private final MongoCollection<Document> collection;

    public TicketRepository(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Ticket insert(Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(new ObjectId());
        }
        collection.insertOne(toDocument(ticket));
        return ticket;
    }

    public Optional<Ticket> findOpenByUserAndType(String userId, TicketType type, String guildId) {
        Document doc = collection.find(
                Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("type", type.name()),
                        Filters.eq("status", TicketStatus.OPEN.name()),
                        Filters.eq("guildId", guildId)
                )
        ).first();
        return Optional.ofNullable(doc).map(this::fromDocument);
    }

    public Optional<Ticket> findByChannelId(String channelId) {
        Document doc = collection.find(Filters.eq("channelId", channelId)).first();
        return Optional.ofNullable(doc).map(this::fromDocument);
    }

    public void close(ObjectId id, String closedBy, Double saleValue) {
        collection.updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                        Updates.set("status", TicketStatus.CLOSED.name()),
                        Updates.set("closedAt", Date.from(Instant.now())),
                        Updates.set("closedBy", closedBy),
                        Updates.set("saleValue", saleValue)
                )
        );
    }

    public List<Ticket> findAllOpen(String guildId) {
        List<Ticket> tickets = new ArrayList<>();
        collection.find(
                Filters.and(
                        Filters.eq("guildId", guildId),
                        Filters.eq("status", TicketStatus.OPEN.name())
                )
        ).forEach(doc -> tickets.add(fromDocument(doc)));
        return tickets;
    }

    private Document toDocument(Ticket ticket) {
        Document doc = new Document();
        doc.put("_id", ticket.getId());
        doc.put("userId", ticket.getUserId());
        doc.put("channelId", ticket.getChannelId());
        doc.put("guildId", ticket.getGuildId());
        doc.put("type", ticket.getType() != null ? ticket.getType().name() : null);
        doc.put("status", ticket.getStatus() != null ? ticket.getStatus().name() : null);
        doc.put("openedAt", ticket.getOpenedAt() != null ? Date.from(ticket.getOpenedAt()) : null);
        doc.put("closedAt", ticket.getClosedAt() != null ? Date.from(ticket.getClosedAt()) : null);
        doc.put("closedBy", ticket.getClosedBy());
        doc.put("saleValue", ticket.getSaleValue());
        doc.put("backupPath", ticket.getBackupPath());
        return doc;
    }

    private Ticket fromDocument(Document doc) {
        Ticket ticket = new Ticket();
        ticket.setId(doc.getObjectId("_id"));
        ticket.setUserId(doc.getString("userId"));
        ticket.setChannelId(doc.getString("channelId"));
        ticket.setGuildId(doc.getString("guildId"));
        String typeStr = doc.getString("type");
        if (typeStr != null) ticket.setType(TicketType.valueOf(typeStr));
        String statusStr = doc.getString("status");
        if (statusStr != null) ticket.setStatus(TicketStatus.valueOf(statusStr));
        Date openedAt = doc.getDate("openedAt");
        if (openedAt != null) ticket.setOpenedAt(openedAt.toInstant());
        Date closedAt = doc.getDate("closedAt");
        if (closedAt != null) ticket.setClosedAt(closedAt.toInstant());
        ticket.setClosedBy(doc.getString("closedBy"));
        ticket.setSaleValue(doc.getDouble("saleValue"));
        ticket.setBackupPath(doc.getString("backupPath"));
        return ticket;
    }
}
