package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static br.com.syrxmc.bot.core.listeners.PermissionsConstants.ALLOWED_PERMISSIONS;
import static br.com.syrxmc.bot.core.listeners.PermissionsConstants.DENIED_PERMISSIONS;

@RegisterCommand
public class AddMemberToTicketCommand extends SlashCommand {

    public AddMemberToTicketCommand() {
        super("add", "Add a user to a ticket");
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.USER, "user", "User to add to the ticket", true));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        Member user = event.getMemberOption("user");

        event.defer().setEphemeral(true).complete().deleteOriginal().queue();

        event.getTextChannel().getManager()
                .putMemberPermissionOverride(user.getIdLong(), ALLOWED_PERMISSIONS, DENIED_PERMISSIONS).queue();

        event.getChannel().sendMessageFormat("%s added to the channel.", user.getAsMention()).queue();
    }
}
