package br.com.syrxmc.bot.utils;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.GoldStock;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.internal.utils.Helpers.isEmpty;

public final class TicketCloser {

    private static final Logger logger = LoggerFactory.getLogger(TicketCloser.class);

    private TicketCloser() {}

    public static void closeCashTicket(Cash.Ticket ticket, Cash cash, String price, TextChannel logs, TextChannel channel, Member author) {
        try {
            WriteChannelBackup.writeFile(channel, "/tickets/" + ticket.type().name());

            if (logs != null && !isEmpty(price)) {
                if (Cash.TicketType.GOLD.equals(ticket.type())) {
                    logs.sendMessageFormat("Venda realizada para <@%s> de **%s** de **GOLD**, por %s",
                            ticket.creatorId(), price, author.getAsMention()).queue();
                } else {
                    logs.sendMessageFormat("Venda realizada para <@%s> de **%s** em **CASH**, por %s",
                            ticket.creatorId(), price, author.getAsMention()).queue();
                }
            }

            cash.removeTicket(ticket);
            Main.getCashManager().save(cash);
            Main.reloadConfig();

            channel.sendMessage("Encerrando ticket em 5s.").queue();
            channel.delete().queueAfter(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            logger.error("Erro ao fechar ticket de cash/intermédio para canal {}", channel.getId(), e);
        }
    }

    public static void closeGoldTicket(String server, Cash.Ticket ticket, Cash cash, long price, TextChannel logs, TextChannel channel, Member author) {
        try {
            WriteChannelBackup.writeFile(channel, "/tickets/" + ticket.type().name());

            if (logs != null && price != 0) {
                logs.sendMessageFormat("Venda realizada para <@%s> de **%s** de **GOLD**, por %s no server **%s**.",
                        ticket.creatorId(), price, author.getAsMention(), server).queue();
            }

            GoldStock goldStock = Main.getGoldStock();
            goldStock.removeStock(channel.getGuild(), server, price);
            Main.getGoldStockDataManager().save(goldStock);

            cash.removeTicket(ticket);
            Main.getCashManager().save(cash);
            Main.reloadConfig();

            channel.sendMessage("Encerrando ticket em 5s.").queue();
            channel.delete().queueAfter(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            logger.error("Erro ao fechar ticket de gold para canal {}", channel.getId(), e);
        }
    }
}