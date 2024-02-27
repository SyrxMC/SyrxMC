package br.com.syrxmc.bot.core.listeners;

import net.dv8tion.jda.api.Permission;

import java.util.List;

public class PermissionsConstants {

    public static final List<Permission> ALLOWED_PERMISSIONS =
            List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EXT_EMOJI, Permission.VIEW_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES);
    public static final List<Permission> DENIED_PERMISSIONS =
            List.of(Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_MENTION_EVERYONE);
}
