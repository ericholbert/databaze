package ericholbert.databaze.database;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import ericholbert.databaze.core.CorePreferences;

public final class DatabasePreferences {

    private final Preferences preferences;
    private final String groupNamesNode = "groupNames";
    private final String lengthClass = "length";

    public DatabasePreferences(String databaseName) {
        preferences = CorePreferences.getPreferences().node(databaseName);
    }

    //TODO: obsah groupName je v novem souboru .plist. Proc? To same viz CorePreferences.
    String[] getGroupNames() {
        int groupNamesSize = preferences.node(groupNamesNode).getInt(lengthClass, 0);
        String[] groupNames = new String[groupNamesSize];
        for (int i = 0; i < groupNamesSize; i++)
            groupNames[i] = preferences.node(groupNamesNode).get(Integer.toString(i), null);
        return groupNames;
    }

    void setGroupNames(String[] groupNamesClass) {
        try {
            String[] keys = preferences.node(groupNamesNode).keys();
            for (String key : keys)
                preferences.node(groupNamesNode).remove(key);
        } catch (BackingStoreException ignored) {
        }
        preferences.node(groupNamesNode).putInt(lengthClass, groupNamesClass.length);
        for (int i = 0; i < groupNamesClass.length; i++)
            preferences.node(groupNamesNode).put(Integer.toString(i), groupNamesClass[i]);
    }

    boolean existsGroupNamesNode() {
        try {
            return preferences.nodeExists(groupNamesNode);
        } catch (BackingStoreException ignored) {
        }
        return false;
    }
    
}
