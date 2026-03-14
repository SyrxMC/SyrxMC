package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.SlashSubcommand;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.GoldStock;
import br.com.syrxmc.bot.utils.TicketCloser;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Optional;

@RegisterCommand
public class CloseStaffCommand extends SlashCommand {

    public CloseStaffCommand() {
        super("fechar-staff", "Fechar a sala de tickets (staff)");
        addSubcommand(new CloseCash());
        addSubcommand(new CloseIntermedio());
        addSubcommand(new CloseGold());
        addRequiredRoles("1352639335039762514");
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        // delegado aos subcomandos
    }

    public static class CloseCash extends SlashSubcommand {

        public CloseCash() {
            super("cash", "Fechar a sala de cash");
            addOption(new OptionData(OptionType.STRING, "valor", "Valor do cash que foi vendido", true));
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            String price = event.getOption("valor").getAsString();
            TextChannel textChannel = event.getChannel().asTextChannel();
            Cash cash = Main.getCashManager().get();

            Optional<Cash.Ticket> ticketOpt = cash.findByChannelId(textChannel.getId());

            if (ticketOpt.isEmpty()) {
                event.reply("Nenhum ticket encontrado neste canal.").setEphemeral(true).queue();
                return;
            }

            Cash.Ticket ticket = ticketOpt.get();

            if (!Cash.TicketType.CASH.equals(ticket.type())) {
                event.reply("O canal que você está tentando fechar não é de cash.").setEphemeral(true).queue();
                return;
            }

            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

            TextChannel logs = event.getGuild().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getCashLogsId());
            TicketCloser.closeCashTicket(ticket, cash, price, logs, textChannel, event.getMember());
        }
    }

    public static class CloseIntermedio extends SlashSubcommand {

        public CloseIntermedio() {
            super("intermedio", "Fechar a sala de intermedio");
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            Cash cash = Main.getCashManager().get();

            Optional<Cash.Ticket> ticketOpt = cash.findByChannelId(textChannel.getId());

            if (ticketOpt.isEmpty()) {
                event.reply("Nenhum ticket encontrado neste canal.").setEphemeral(true).queue();
                return;
            }

            Cash.Ticket ticket = ticketOpt.get();

            if (!Cash.TicketType.INTERMEDIO.equals(ticket.type())) {
                event.reply("O canal que você está tentando fechar não é de intermédio.").setEphemeral(true).queue();
                return;
            }

            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

            TextChannel logs = event.getGuild().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getCashLogsId());
            TicketCloser.closeCashTicket(ticket, cash, null, logs, textChannel, event.getMember());
        }
    }

    public static class CloseGold extends SlashSubcommand {

        public CloseGold() {
            super("gold", "Fechar a sala de gold");
            addOption(new OptionData(OptionType.STRING, "servidor", "Servidor da venda", true));
            addOption(new OptionData(OptionType.INTEGER, "valor", "Valor de gold vendida", true));
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            String server = event.getOption("servidor").getAsString();
            long valor = event.getOption("valor").getAsLong();

            GoldStock stock = Main.getGoldStockDataManager().get();
            if (!stock.getGoldStock().containsKey(server)) {
                event.reply("Não há estoque para esse servidor.").setEphemeral(true).queue();
                return;
            }

            TextChannel textChannel = event.getChannel().asTextChannel();
            Cash cash = Main.getCashManager().get();

            Optional<Cash.Ticket> ticketOpt = cash.findByChannelId(textChannel.getId());

            if (ticketOpt.isEmpty()) {
                event.reply("Nenhum ticket encontrado neste canal.").setEphemeral(true).queue();
                return;
            }

            Cash.Ticket ticket = ticketOpt.get();

            if (!Cash.TicketType.GOLD.equals(ticket.type())) {
                event.reply("O canal que você está tentando fechar não é de gold.").setEphemeral(true).queue();
                return;
            }

            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

            TextChannel logs = event.getGuild().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getGoldLogsId());
            TicketCloser.closeGoldTicket(server, ticket, cash, valor, logs, textChannel, event.getMember());
        }
    }
}