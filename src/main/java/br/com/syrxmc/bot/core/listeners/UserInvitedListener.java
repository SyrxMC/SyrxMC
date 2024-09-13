package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class UserInvitedListener extends DynamicHandler<GuildMemberJoinEvent> {

    public UserInvitedListener() {
        super(guildMemberJoinEvent -> true);
    }

    @Override
    public void onEvent(GuildMemberJoinEvent event) {
    }
}
