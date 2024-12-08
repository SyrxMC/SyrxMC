package br.com.syrxmc.bot.core.listeners.invite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class InviteUserCache {

    private final String userId;
    private final String code;
}
