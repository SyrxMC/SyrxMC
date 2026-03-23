package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.invite.InviteService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;

@RegisterCommand
public class CreateInviteCommand extends SlashCommand {

    public CreateInviteCommand() {
        super("criar-invite", "Cria um invite para o canal público do servidor");
        addPermissions(Permission.MANAGE_SERVER);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String guildId = event.getGuild().getId();

        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();
        GuildConfig config = guildConfigService.getConfig(guildId);

        String inviteChannelId = config.getChannels() != null ? config.getChannels().getInvite() : null;
        if (inviteChannelId == null) {
            event.reply("Canal público não configurado.").setEphemeral(true).queue();
            return;
        }

        StandardGuildChannel channel = event.getGuild().getChannelById(StandardGuildChannel.class, inviteChannelId);
        if (channel == null) {
            event.reply("Canal público não encontrado.").setEphemeral(true).queue();
            return;
        }

        channel.createInvite().queue(invite -> {
            InviteService inviteService = ServiceRegistry.getInviteService();
            inviteService.upsertInviter(guildId, invite.getCode(), event.getAuthor().getId());

            event.reply("https://discord.gg/" + invite.getCode()).setEphemeral(true).queue();
        }, error -> event.reply("Erro ao criar o invite: " + error.getMessage()).setEphemeral(true).queue());
    }
}
