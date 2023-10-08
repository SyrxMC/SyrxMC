package br.com.syrxmc.bot.data;

import com.google.gson.annotations.JsonAdapter;
import lombok.*;

@Data
public class Config {

    private String token;

    private String greetingChannelId;

    private String discordInvite;

    private String siteUrl;

}
