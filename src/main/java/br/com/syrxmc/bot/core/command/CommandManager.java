package br.com.syrxmc.bot.core.command;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor;

@Getter
public class CommandManager {

    private final Map<String, SlashCommand> commands = new HashMap<>();
    private final List<SlashCommandData> commandData = new ArrayList<>();

    private final JDA jda;

    @SneakyThrows
    public CommandManager(JDA jda) {
        this.jda = jda;
    }

    {
        findCommands();
    }

    public void publicCommands() {
        jda.updateCommands().addCommands(commandData).queue();
    }

    public SlashCommand getCommand(String commandName) {
        return this.getCommands().get(commandName);
    }


    @SneakyThrows
    private void findCommands() {
        Reflections reflections = new Reflections(Main.class.getPackageName() + ".commands");

        for (Class<?> aClass : reflections.getTypesAnnotatedWith(RegisterCommand.class)) {
            registerCommands((SlashCommand) aClass.getConstructor().newInstance());
        }
    }

    public void registerCommands(SlashCommand command) {
        if (command != null) {
            getCommands().put(command.getName(), command);
            commandData.add(Commands.slash(command.getName(), command.getDescription()).addOptions(command.getOptions())
                    .addSubcommands(getSubCommandData(command)).setDefaultPermissions(enabledFor(command.getPermissions())));
        }
    }

    private List<SubcommandData> getSubCommandData(SlashCommand iCommand) {
        return new ArrayList<>(iCommand.getSubcommands().entrySet().stream()
                .map(entry -> new SubcommandData(entry.getKey(), entry.getValue().getDescription())
                        .addOptions(entry.getValue().getOptions()))
                .toList());
    }
}
