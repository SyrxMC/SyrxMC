package br.com.syrxmc.bot.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Invites {

    private String lastMessageId;

    private boolean active;

    private Map<String, InviteData> invites = new HashMap<>();

    private List<User> users = new ArrayList<>();


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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String id;

        private String username;

        private String invitedCode;
    }

    public void createInvite(String userId, String inviteCode){
        invites.putIfAbsent(inviteCode, new InviteData(userId));
    }

    public void createUser(User user){
        this.users.add(user);
    }

    public void deleteUser(User user){
        this.users.remove(user);
    }

    public void deleteInvite(String code){
      invites.remove(code);
    }

    public void deleteInvite(List<String> code){
        code.forEach(s -> invites.remove(s));
    }

    public InviteData getInvite(String code){
        return invites.get(code);
    }

    public InviteData incrementInvite(String inviteCode, String userId){
        InviteData inviteData = invites.get(inviteCode);

        inviteData.addInvitedUser(userId);
        inviteData.incrementCount();

        return inviteData;
    }

    public InviteData decrementInvite(String inviteCode, String userId){
        InviteData inviteData = invites.get(inviteCode);

        inviteData.removeInvitedUser(userId);
        inviteData.decrementCount();

        return inviteData;
    }

    public List<String> getCodeByUserId(String userId){
        List<String> invList = invites.entrySet().stream().filter(stringInviteDataEntry -> stringInviteDataEntry.getValue().getUserId().equals(userId))
                .map(stringInviteDataEntry -> stringInviteDataEntry.getKey()).collect(Collectors.toList());
        return invList;
    }

}
