package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;

public class MemberJoinListener extends DynamicHandler<GuildMemberJoinEvent> {

    public MemberJoinListener() {
        super(event -> true);
    }

    @Override
    public void onEvent(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getGuild().getChannelById(TextChannel.class, "1160278425743937627");

        String author = String.format("%s(%s)", event.getMember().getEffectiveName(), event.getMember().getId());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(author);
        embed.setTitle(String.format("\uD83C\uDF89 Seja bem vindo(a) %s, ao SyrxMC\n", member.getEffectiveName()));
        embed.setDescription("Somos uma comunidade apaixonada por Minecraft com MODS, dedicada a proporcionar uma experiência de jogo incrível para todos os jogadores. Nosso servidor de Minecraft oferece uma jogabilidade única, uma comunidade acolhedora e eventos emocionantes.\n" +
                "\n" +
                "Você precisa passar por uma verificação para sabermos se você não é um robô, mas antes disso leia nossas <#1160278506421358643> para que não ocorra nenhuma problema futuro.\n" +
                "\n" +
                "\uD83C\uDF10 Site: [link](https://google.com)\n" +
                "<:discord:1160410448160620665> Discord: [link](https://discord.gg/SryxMC)");
        embed.setFooter("SyrxMC - Sempre trazendo o melhor para a sua diversão!");
        embed.setColor(Color.cyan);

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
