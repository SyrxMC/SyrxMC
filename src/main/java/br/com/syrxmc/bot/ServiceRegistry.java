package br.com.syrxmc.bot;

import br.com.syrxmc.bot.domain.gold.GoldStockService;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.invite.InviteService;
import br.com.syrxmc.bot.domain.ticket.TicketService;

/**
 * Static service registry to allow commands (instantiated via reflection with no-arg constructors)
 * to access the application's domain services.
 */
public class ServiceRegistry {

    private static TicketService ticketService;
    private static GoldStockService goldStockService;
    private static InviteService inviteService;
    private static GuildConfigService guildConfigService;

    private ServiceRegistry() {}

    public static void register(
            TicketService ticketService,
            GoldStockService goldStockService,
            InviteService inviteService,
            GuildConfigService guildConfigService
    ) {
        ServiceRegistry.ticketService = ticketService;
        ServiceRegistry.goldStockService = goldStockService;
        ServiceRegistry.inviteService = inviteService;
        ServiceRegistry.guildConfigService = guildConfigService;
    }

    public static TicketService getTicketService() {
        ensureRegistered(ticketService, "TicketService");
        return ticketService;
    }

    public static GoldStockService getGoldStockService() {
        ensureRegistered(goldStockService, "GoldStockService");
        return goldStockService;
    }

    public static InviteService getInviteService() {
        ensureRegistered(inviteService, "InviteService");
        return inviteService;
    }

    public static GuildConfigService getGuildConfigService() {
        ensureRegistered(guildConfigService, "GuildConfigService");
        return guildConfigService;
    }

    private static void ensureRegistered(Object service, String name) {
        if (service == null) {
            throw new IllegalStateException(name + " has not been registered. Call ServiceRegistry.register() first.");
        }
    }
}
