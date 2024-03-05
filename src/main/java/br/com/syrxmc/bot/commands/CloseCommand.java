package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.SlashSubcommand;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.utils.WriteChannelBackup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static net.dv8tion.jda.internal.utils.Helpers.isEmpty;

@RegisterCommand
public class CloseCommand extends SlashCommand {

    public CloseCommand() {
        super("fechar", "Fechar a salas de tickets");
        addSubcommand(new CloseCash());
        addSubcommand(new CloseIntermedio());
        addSubcommand(new CloseGold());
        addPermissions(Permission.ADMINISTRATOR);
    }

    public static void closeChannel(Cash.Ticket ticket, Cash cash, String price, TextChannel logs, TextChannel channel, Member author) {

        try {

            WriteChannelBackup.writeFile(channel, "/tickets/" + ticket.type().name());

            if (!isEmpty(price)) {
                if (!isNull(ticket)) {
                    logs.sendMessageFormat("Venda realizada para <@%s> de **%s** em **CASH**, por %s", ticket.creatorId(), price, author.getAsMention()).queue();
                } else {
                    author.getUser().openPrivateChannel().complete().sendMessage("Deu um erro ao enviar a mensagem para o canal de logs favor contatar os devs.").queue();
                }
            }

            if (!isNull(ticket)) {
                List<Cash.Ticket> tickets = cash.getTickets().get(ticket.creatorId());

                tickets.remove(ticket);
                cash.getTickets().put(ticket.creatorId(), tickets);

                Main.getCashManager().save(cash);
                Main.reloadConfig();

            } else {
                author.getUser().openPrivateChannel().complete().sendMessage("Deu um erro ao enviar a mensagem para o canal de logs favor contatar os devs.").queue();
            }

            channel.sendMessage("Encerrando ticket em 5s.").queue();
            channel.delete().queueAfter(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        // do nothing
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

            Cash.Ticket ticket = null;

            for (List<Cash.Ticket> value : cash.getTickets().values()) {
                for (Cash.Ticket ticket1 : value) {
                    if (ticket1.channelId().equals(textChannel.getId())) {
                        ticket = ticket1;
                        break;
                    }
                }
            }

            if (!isNull(ticket) && !Cash.TicketType.CASH.equals(ticket.type())) {
                event.reply("O canal que você está tentando fechar não é de cash").setEphemeral(true).queue();
                return;
            }

            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
            closeChannel(ticket, cash, price, event.getGuild().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getCashLogsId()), textChannel, event.getMember());
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

            Cash.Ticket ticket = null;

            for (List<Cash.Ticket> value : cash.getTickets().values()) {
                for (Cash.Ticket ticket1 : value) {
                    if (ticket1.channelId().equals(textChannel.getId())) {
                        ticket = ticket1;
                        break;
                    }
                }
            }

            if (!isNull(ticket) && !Cash.TicketType.INTERMEDIO.equals(ticket.type())) {
                event.reply("O canal que você está tentando fechar não é de intermédio").setEphemeral(true).queue();
                return;
            }

            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
            closeChannel(ticket, cash, null, event.getGuild().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getCashLogsId()), textChannel, event.getMember());
        }
    }

    public static class CloseGold extends SlashSubcommand {

        public CloseGold() {
            super("gold", "Fechar a sala de gold");
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {

            TextChannel textChannel = event.getChannel().asTextChannel();

            Cash cash = Main.getCashManager().get();

            Cash.Ticket ticket = null;

            for (List<Cash.Ticket> value : cash.getTickets().values()) {
                for (Cash.Ticket ticket1 : value) {
                    if (ticket1.channelId().equals(textChannel.getId())) {
                        ticket = ticket1;
                        break;
                    }
                }
            }

            if (!isNull(ticket) && !Cash.TicketType.GOLD.equals(ticket.type())) {
                event.reply("O canal que você está tentando fechar não é de gold").setEphemeral(true).queue();
                return;
            }

            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
            closeChannel(ticket, cash, null, event.getGuild().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getCashLogsId()), textChannel, event.getMember());
        }
    }

}
