package br.com.syrxmc.bot.core.listeners.invite;

import net.dv8tion.jda.api.entities.Invite;

public class InviteData {

    private final long guildId;
    private int uses;
    private final String code;
    private final String userName;

    public InviteData(Invite invite) {
        this.guildId = invite.getGuild().getIdLong();
        this.uses = invite.getUses();
        this.code = invite.getCode();
        this.userName = invite.getInviter() != null ? invite.getInviter().getAsTag() : "NoName";
    }

    public String getUserName() {
        return userName;
    }

    public String getCode() {
        return code;
    }

    public int getUses() {
        return uses;
    }

    public long getGuildId() {
        return guildId;
    }

    public void removeUses() {
        this.uses--;
    }

    public void incrementUses() {
        this.uses++;
    }
}
