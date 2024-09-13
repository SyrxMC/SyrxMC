package br.com.syrxmc.bot.data;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
public class Invites {

    private String lastMessageId;

    private Map<String, InviteData> invites = new HashMap<>();


    @Getter
    @NoArgsConstructor
    public static class InviteData {

        private String userId;

        private Long count;

        private List<String> invitedUsersId = new ArrayList<>();


        public InviteData(String userId){
            this.userId = userId;
            this.count = 0L;
        }

        public void incrementCount(){
            this.count++;
        }

        public void decrementCount(){
            this.count--;
        }

        public void addInvitedUser(String userId){
            invitedUsersId.add(userId);
        }

        public void removeInvitedUser(String userId){
            invitedUsersId.remove(userId);
        }
    }

    public void createInvite(String userId, String inviteCode){
        invites.putIfAbsent(inviteCode, new InviteData(userId));
    }

    public void deleteInvite(String code){
      invites.remove(code);
    }

    public InviteData getInvite(String code){
        return invites.get(code);
    }

    public InviteData incrementInvite(String inviteCode){
        InviteData inviteData = invites.get(inviteCode);

        inviteData.incrementCount();

        return inviteData;
    }

    public InviteData decrementInvite(String inviteCode){
        InviteData inviteData = invites.get(inviteCode);

        inviteData.incrementCount();

        return inviteData;
    }

    public String getCodeByUserId(String userId){
        Optional<Map.Entry<String, InviteData>> optionalInvite = invites.entrySet().stream().filter(stringInviteDataEntry -> stringInviteDataEntry.getValue().getUserId().equals(userId))
                .findFirst();
        return optionalInvite.map(Map.Entry::getKey).orElse(null);
    }

}
