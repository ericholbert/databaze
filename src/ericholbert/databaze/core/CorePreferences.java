package ericholbert.databaze.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import ericholbert.databaze.database.Database;

final public class CorePreferences {

    private static final Preferences preferences = Preferences.userRoot().node("ericholbert.databaze");
    private static final String CORE_NODE = "core";
    private static final String ROOT_PATH_CLASS = "rootPath";
    private static final String LAST_DATABASE_NAME_CLASS = "lastDatabaseName";
    private static final String WINDOW_SIZE_NODE = "windowSize";
    private static final String WIDTH_CLASS = "width";

    public static Preferences getPreferences() {
        return preferences;
    }

    public static Path getRootPath() {
        String rootPath = preferences.node(CORE_NODE).get(ROOT_PATH_CLASS, System.getProperty("user.home"));
        rootPath += "/" + Database.ROOT_NAME;
        return Path.of(rootPath);
    }

    public static String getLastDatabaseName() {
        return preferences.node(CORE_NODE).get(LAST_DATABASE_NAME_CLASS, null);
    }

    public static void setLastDatabaseName(String lastDatabaseNameClass) {
        preferences.node(CORE_NODE).put(LAST_DATABASE_NAME_CLASS, lastDatabaseNameClass);
    }

    public static int getWindowWidth() {
        return preferences.node(CORE_NODE).node(WINDOW_SIZE_NODE).getInt(WIDTH_CLASS, 80);
    }

    static void setRootPath(String path) {
        preferences.node(CORE_NODE).put(ROOT_PATH_CLASS, path);
    }

    static boolean isRootPathValid(String path) {
        return Files.isDirectory(Path.of(path));
    }

    static boolean setWindowWidth(int width) {
        if (width < 80)
            return false;
        preferences.node(CORE_NODE).node(WINDOW_SIZE_NODE).putInt(WIDTH_CLASS, width);
        return true;
    }

}
