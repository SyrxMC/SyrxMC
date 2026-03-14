package br.com.syrxmc.bot.data;

import lombok.Data;

import java.util.List;

@Data
public class Config {

    private String menuChannel;

    private String token;

    private String cashCategory;

    private String infoChannel;

    private List<String> casherIds;

    private String goldId;

    private String greetingChannelId;

    private String inviteChannel;

    private String cashCategoryId;

    private String ticketOpenMessage;

    private String intermedioOpenMessage;

    private String goldOpenMessage;

    private String cashLogsId;

    private String goldLogsId;

    private String image;

    // Cargo de staff que pode usar comandos de staff (opcional)
    private String staffRoleId;

}
