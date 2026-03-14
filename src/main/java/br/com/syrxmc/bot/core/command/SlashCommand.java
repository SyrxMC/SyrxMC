package br.com.syrxmc.bot.core.command;

import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class SlashCommand {

    private final String name;
    private final String description;
    private final boolean isGuildOnly;
    private final List<OptionData> options;
    private final List<Permission> permissions;
    private final List<String> requiredRoleIds;

    @Getter
    private final Map<String, SlashSubcommand> subcommands;

    public SlashCommand(String name, String description) {
        this.name = name;
        isGuildOnly = false;
        this.description = description;
        this.options = new ArrayList<>();
        this.subcommands = new HashMap<>();
        this.permissions = new ArrayList<>();
        this.requiredRoleIds = new ArrayList<>();
    }

    public SlashCommand(String name, String description, boolean isGuildOnly) {
        this.name = name;
        this.isGuildOnly = isGuildOnly;
        this.description = description;
        this.options = new ArrayList<>();
        this.subcommands = new HashMap<>();
        this.permissions = new ArrayList<>();
        this.requiredRoleIds = new ArrayList<>();
    }


    public void addPermissions(Permission... permissions) {
        this.permissions.addAll(List.of(permissions));
    }

    public void addRequiredRoles(String... roleIds) {
        if (roleIds == null) return;
        for (String id : roleIds) {
            if (id != null && !id.isBlank()) {
                this.requiredRoleIds.add(id);
            }
        }
    }

    public void addSubcommand(SlashSubcommand subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
    }

    public void addOption(OptionData option) {
        options.add(option);
    }

    public abstract void execute(SlashCommandEvent event) throws Exception;

}
