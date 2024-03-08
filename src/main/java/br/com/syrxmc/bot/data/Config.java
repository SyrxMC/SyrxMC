package br.com.syrxmc.bot.data;

import lombok.Data;

import java.util.List;

@Data
public class Config {

    private String token;

    private String cashCategory;

    private String infoChannel;

    private List<String> casherIds;

    private String greetingChannelId;

    private String discordInvite;

    private String cashCategoryId;

    private String ticketOpenMessage;

    private String intermedioOpenMessage;

    private String goldOpenMessage;

    private String cashLogsId;

    private String goldLogsId;

}
