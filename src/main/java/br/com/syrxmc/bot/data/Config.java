package br.com.syrxmc.bot.data;

import lombok.Data;

@Data
public class Config {

    private String token;

    private String clientRoleId;

    private String guildId;

    private String serverRole;

    private String goldRole;

    private String announcementRole;

    private String announcementChannel;

}
