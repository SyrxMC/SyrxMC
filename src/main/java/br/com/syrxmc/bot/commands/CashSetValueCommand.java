package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterCommand
public class CashSetValueCommand extends SlashCommand {

    public CashSetValueCommand() {
        super("cashvalor", "Define o valor diário do cash", true);
        addRequiredRoles("1352639335039762514", "1240266588381380611");
        addOption(new OptionData(OptionType.STRING, "valor", "Valor do cash (ex.: 5,20)", true));
        addOption(new OptionData(OptionType.STRING, "dolar", "Valor do cash em dol (ex.: 15,20)", true));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String value = event.getStringOption("valor");
        String dolar = event.getStringOption("dolar");

        Main.getCashDaily().setValue(value);
        Main.getCashDaily().setDolar(dolar);
        Main.getCashDailyManager().save(Main.getCashDaily());

        event.reply("Valor do cash atualizado para: %s, Valor do dolar: %s".formatted(value, dolar)).setEphemeral(true).queue();
    }
}
