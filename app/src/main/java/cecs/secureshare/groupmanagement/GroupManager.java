package cecs.secureshare.groupmanagement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Douglas on 12/5/2015.
 */
public class GroupManager {

    private static GroupManager mInstance = new GroupManager();

    public static GroupManager getInstance() {
        return mInstance;
    }

    private Map<String, GroupMember> groupMembers;

    private GroupManager() {
        this.groupMembers = new HashMap<String, GroupMember>();
    }

    /**
     * Add a group member to the map
     * @param member
     */
    public void addGroupMember(String id, GroupMember member) {
        groupMembers.put(id, member);
    }

    /**
     * Remove a group member from the map
     * @param id - the key to identify the member
     */
    public void deleteGroupMember(String id) {
        groupMembers.remove(id);
    }

    /**
     * Removes all members from the group
    */
    public void clearGroup() {
        groupMembers.clear();
    }

    /**
     * Returns the number of group members
     * @return
     */
    public int getGroupSize() {
        return groupMembers.size();
    }

    /**
     * The map of group member id to group members
     * @return
     */
    public Map<String, GroupMember> getGroupMembers() {
        return groupMembers;
    }
}
