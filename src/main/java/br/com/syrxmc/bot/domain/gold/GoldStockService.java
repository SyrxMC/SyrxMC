package br.com.syrxmc.bot.domain.gold;

import br.com.syrxmc.bot.database.repositories.GoldStockRepository;
import br.com.syrxmc.bot.database.repositories.GuildConfigRepository;

import java.util.List;

public class GoldStockService {

    private final GoldStockRepository goldStockRepository;
    private final GuildConfigRepository guildConfigRepository;

    public GoldStockService(GoldStockRepository goldStockRepository, GuildConfigRepository guildConfigRepository) {
        this.goldStockRepository = goldStockRepository;
        this.guildConfigRepository = guildConfigRepository;
    }

    public void add(String guildId, String serverName, long amount, String updatedBy) {
        goldStockRepository.upsert(guildId, serverName, amount, updatedBy);
    }

    public void remove(String guildId, String serverName, long amount, String updatedBy) {
        goldStockRepository.remove(guildId, serverName, amount, updatedBy);
    }

    public List<GoldStock> getAll(String guildId) {
        return goldStockRepository.findAll(guildId);
    }
}
