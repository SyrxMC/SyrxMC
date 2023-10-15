package br.com.syrxmc.bot.data;

import lombok.Data;

@Data
public class Config {

    private String token;

    private String greetingChannelId;

    private String discordInvite;

    private String siteUrl;

    private String ticketCategoryId;

    private String suggestionChannelId;

    private String systemLogChannelId;

}
